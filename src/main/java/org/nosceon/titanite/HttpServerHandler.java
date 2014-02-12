package org.nosceon.titanite;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.nosceon.titanite.Response.internalServerError;

/**
 * @author Johan Siebens
 */
class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final Router router;

    public HttpServerHandler(Router router) {
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder qsd = new QueryStringDecoder(request.getUri());
        RoutingResult routingResult = router.find(qsd.path());

        Request req =
            new Request(
                request.getMethod(),
                qsd.path(),
                new HeaderParams(request.headers()),
                new PathParams(routingResult.pathParams),
                new QueryParams(qsd.parameters()),
                getRequestBody(request)
            );

        Function<Request, Response> function = routingResult.selector.get(req.method, req.path);

        try {
            Optional
                .of(function.apply(req))
                .ifPresent(r -> r.apply(request, ctx));
        }
        catch (Exception e) {
            logger.error("error processing request", e);
            internalServerError().apply(request, ctx);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("error handling request", cause);
        ctx.channel().close();
    }

    private RequestBody getRequestBody(FullHttpRequest request) {
        return new DefaultRequestBody(request.content());
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

    private static class HeaderParams extends Params {

        private HttpHeaders headers;

        private HeaderParams(HttpHeaders headers) {
            this.headers = headers;
        }

        @Override
        public Optional<String> getString(String name) {
            return Optional.ofNullable(headers.get(name));
        }

    }

    private static class PathParams extends Params {

        private Map<String, String> values;

        private PathParams(Map<String, String> values) {
            this.values = values;
        }

        @Override
        public Optional<String> getString(String name) {
            return Optional.ofNullable(values.get(name));
        }

    }

    private static class QueryParams extends Params {

        private Map<String, List<String>> values;

        private QueryParams(Map<String, List<String>> values) {
            this.values = values;
        }

        @Override
        public Optional<String> getString(String name) {
            return
                Optional.ofNullable(values.get(name))
                    .filter(l -> !l.isEmpty())
                    .map((l) -> l.get(0));
        }

    }

}
