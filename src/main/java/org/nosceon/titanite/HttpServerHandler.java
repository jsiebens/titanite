package org.nosceon.titanite;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
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

    private final ViewRenderer renderer;

    private final ObjectMapper mapper;

    private HttpRequest request;

    private Aggregator aggregator;

    private int maxRequestSize;

    private boolean tooLongFrameFound;

    public HttpServerHandler(int maxRequestSize, Router router, ViewRenderer renderer, ObjectMapper mapper) {
        this.maxRequestSize = maxRequestSize;
        this.router = router;
        this.renderer = renderer;
        this.mapper = mapper;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            this.request = (HttpRequest) msg;
            this.aggregator = newAggregator(request, ctx);

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

            if (aggregator.length() > maxRequestSize - chunk.content().readableBytes()) {
                tooLongFrameFound = true;

                // release current message to prevent leaks
                aggregator.release();
                aggregator = null;

                ctx.writeAndFlush(TOO_LARGE).addListener(ChannelFutureListener.CLOSE);
                return;
            }

            // Append the content of the chunk
            if (chunk.content().isReadable()) {
                aggregator.offer(chunk);
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
                        new HeaderParams(request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, aggregator.length())),
                        new CookieParams(cookies),
                        new PathParams(routing.pathParams),
                        new QueryParams(qsd.parameters()),
                        aggregator.newRequestBody()
                    );

                Response response = null;
                try {
                    response = routing.function.apply(req);
                }
                catch (HttpServerException e) {
                    logger.error("error processing request", e);
                    response = e.getResponse();
                }
                catch (Exception e) {
                    logger.error("error processing request", e);
                    ctx.writeAndFlush(INTERNAL_SERVER_ERROR).addListener(ChannelFutureListener.CLOSE);
                }

                Optional.ofNullable(response).ifPresent((r) -> r.apply(request, ctx, renderer, mapper));
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("error handling request", cause);
        ctx.channel().close();
    }

    private Aggregator newAggregator(HttpRequest request, ChannelHandlerContext ctx) {
        String contentType = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);
        if (contentType != null) {
            HttpMethod method = request.getMethod();
            String lowerCaseContentType = contentType.toLowerCase();
            boolean isURLEncoded = lowerCaseContentType.startsWith(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);
            boolean isMultiPart = lowerCaseContentType.startsWith(HttpHeaders.Values.MULTIPART_FORM_DATA);

            if ((isMultiPart || isURLEncoded) &&
                (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) || method.equals(HttpMethod.PATCH))) {

                return new FormAggregator(maxRequestSize, new HttpPostRequestDecoder(request));
            }
        }
        return new DefaultAggregator(maxRequestSize, ctx.alloc().compositeBuffer());
    }

    private static interface Aggregator {

        boolean maxRequestSizeExceeded(HttpContent chunk);

        long length();

        void offer(HttpContent chunk);

        void release();

        RequestBody newRequestBody();

    }

    private static class DefaultAggregator implements Aggregator {

        private long maxRequestSize;

        private CompositeByteBuf content;

        private DefaultAggregator(long maxRequestSize, CompositeByteBuf content) {
            this.maxRequestSize = maxRequestSize;
            this.content = content;
        }

        @Override
        public boolean maxRequestSizeExceeded(HttpContent chunk) {
            return content.readableBytes() > maxRequestSize - chunk.content().readableBytes();
        }

        @Override
        public long length() {
            return content.readableBytes();
        }

        @Override
        public void offer(HttpContent chunk) {
            chunk.retain();
            content.addComponent(chunk.content());
            content.writerIndex(content.writerIndex() + chunk.content().readableBytes());
        }

        @Override
        public void release() {
            content.release();
        }

        @Override
        public RequestBody newRequestBody() {
            return new DefaultRequestBody(content);
        }

    }

    private static class FormAggregator implements Aggregator {

        private long maxRequestSize;

        private long length;

        private HttpPostRequestDecoder decoder;

        private FormAggregator(long maxRequestSize, HttpPostRequestDecoder decoder) {
            this.maxRequestSize = maxRequestSize;
            this.decoder = decoder;
        }

        @Override
        public boolean maxRequestSizeExceeded(HttpContent chunk) {
            return length > maxRequestSize - chunk.content().readableBytes();
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public void offer(HttpContent chunk) {
            decoder.offer(chunk);
            length += chunk.content().readableBytes();
        }

        @Override
        public void release() {
            decoder.destroy();
        }

        @Override
        public RequestBody newRequestBody() {
            return new FormRequestBody(decoder);
        }
    }

    private static class DefaultRequestBody implements RequestBody {

        private ByteBuf content;

        private DefaultRequestBody(ByteBuf content) {
            this.content = content;
        }

        @Override
        public boolean isForm() {
            return false;
        }

        @Override
        public InputStream asStream() {
            return new ByteBufInputStream(content);
        }

        @Override
        public FormParams asForm() {
            throw new UnsupportedOperationException("asForm not supported");
        }

    }

    private static class FormRequestBody implements RequestBody {

        private FormParams form;

        private FormRequestBody(HttpPostRequestDecoder decoder) {
            this.form = new FormParams(decoder);
        }

        @Override
        public boolean isForm() {
            return true;
        }

        @Override
        public InputStream asStream() {
            throw new UnsupportedOperationException("asStream not supported");
        }

        @Override
        public FormParams asForm() {
            return form;
        }

    }

}
