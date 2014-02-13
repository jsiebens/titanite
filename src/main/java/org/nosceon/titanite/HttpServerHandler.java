package org.nosceon.titanite;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static java.util.stream.Collectors.toMap;

/**
 * @author Johan Siebens
 */
class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final FullHttpResponse CONTINUE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);

    private static final FullHttpResponse TOO_LARGE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, Unpooled.EMPTY_BUFFER);

    private static final FullHttpResponse INTERNAL_SERVER_ERROR = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.EMPTY_BUFFER);

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final Router router;

    private HttpRequest request;

    private CompositeByteBuf content;

    private int maxRequestSize;

    private boolean tooLongFrameFound;

    public HttpServerHandler(int maxRequestSize, Router router) {
        this.maxRequestSize = maxRequestSize;
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            this.request = (HttpRequest) msg;
            this.content = ctx.alloc().compositeBuffer();

            if (is100ContinueExpected(request)) {
                ctx.writeAndFlush(CONTINUE).addListener(future -> {
                    if (!future.isSuccess()) {
                        ctx.fireExceptionCaught(future.cause());
                    }
                });
            }
        }

        if (msg instanceof HttpContent) {
            if (tooLongFrameFound) {
                return;
            }

            HttpContent chunk = (HttpContent) msg;

            if (content.readableBytes() > maxRequestSize - chunk.content().readableBytes()) {
                tooLongFrameFound = true;

                // release current message to prevent leaks
                content.release();
                content = null;

                ctx.writeAndFlush(TOO_LARGE).addListener(ChannelFutureListener.CLOSE);
                return;
            }

            // Append the content of the chunk
            if (chunk.content().isReadable()) {
                chunk.retain();
                content.addComponent(chunk.content());
                content.writerIndex(content.writerIndex() + chunk.content().readableBytes());
            }

            if (chunk instanceof LastHttpContent) {
                QueryStringDecoder qsd = new QueryStringDecoder(request.getUri());
                RoutingResult routing = router.find(request.getMethod(), qsd.path());

                Map<String, CookieParam> cookies = Optional.ofNullable(request.headers().get(COOKIE))
                    .map(CookieDecoder::decode)
                    .map(s -> s.stream().collect(toMap(io.netty.handler.codec.http.Cookie::getName, CookieParam::new)))
                    .orElseGet(Collections::emptyMap);

                Request req =
                    new Request(
                        request.getMethod(),
                        qsd.path(),
                        new HeaderParams(request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes())),
                        new CookieParams(cookies),
                        new PathParams(routing.pathParams),
                        new QueryParams(qsd.parameters()),
                        new DefaultRequestBody(content)
                    );

                try {
                    Optional
                        .of(routing.function.apply(req))
                        .ifPresent(r -> r.apply(request, ctx));

                    content.release();
                }
                catch (Exception e) {
                    logger.error("error processing request", e);
                    ctx.writeAndFlush(INTERNAL_SERVER_ERROR).addListener(ChannelFutureListener.CLOSE);
                }
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("error handling request", cause);
        ctx.channel().close();
    }

    private static class DefaultRequestBody implements RequestBody {

        private ByteBuf content;

        private DefaultRequestBody(ByteBuf content) {
            this.content = content;
        }

        @Override
        public InputStream asStream() {
            return new ByteBufInputStream(content);
        }

    }

}
