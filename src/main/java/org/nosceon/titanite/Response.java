package org.nosceon.titanite;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.nosceon.titanite.HttpServerException.propagate;
import static org.nosceon.titanite.Responses.internalServerError;

/**
 * @author Johan Siebens
 */
public final class Response {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public static final Charset UTF8 = Charset.forName("UTF8");

    private HttpResponseStatus status;

    private HttpHeaders headers = new DefaultHttpHeaders();

    private Body body = new DefaultBody(Unpooled.EMPTY_BUFFER);

    Response(HttpResponseStatus status) {
        this.status = status;
    }

    public Response header(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public Response cookie(String name, String value) {
        headers.add(SET_COOKIE, new Cookie(name, value).encode());
        return this;
    }

    public Response cookie(Cookie cookie) {
        headers.add(SET_COOKIE, cookie.encode());
        return this;
    }

    public Response text(String content) {
        this.body = new DefaultBody(Unpooled.copiedBuffer(content, UTF8));
        return this;
    }

    public Response stream(StreamingOutput consumer) {
        this.body = new StreamBody(consumer);
        return this;
    }

    public Response view(View view) {
        this.body = new ViewBody(view);
        return this;
    }

    public Response json(Object entity) {
        this.body = new JsonBody(entity);
        return this;
    }

    void apply(HttpRequest request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, ObjectMapper mapper) {
        body.apply(request, ctx, viewRenderer, mapper);
    }

    private static interface Body {

        void apply(HttpRequest request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, ObjectMapper mapper);

    }

    private class DefaultBody implements Body {

        private ByteBuf content;

        private DefaultBody(ByteBuf content) {
            this.content = content;
        }

        @Override
        public void apply(HttpRequest request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, ObjectMapper mapper) {
            boolean keepAlive = isKeepAlive(request);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, content);
            response.headers().add(headers);

            if (keepAlive) {
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            ChannelFuture future = ctx.writeAndFlush(response);
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

    }

    private abstract class AbstractStreamingBody implements Body {

        protected void stream(ChannelHandlerContext ctx, StreamingOutput consumer1) {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
            response.headers().add(headers);
            HttpHeaders.setTransferEncodingChunked(response);
            ctx.write(response);
            propagate(() -> {
                try (OutputStream out = new ChunkOutputStream(ctx, 1024)) {
                    consumer1.apply(out);
                }
                return true;
            });
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
        }

    }

    private class StreamBody extends AbstractStreamingBody {

        private StreamingOutput consumer;

        private StreamBody(StreamingOutput consumer) {
            this.consumer = consumer;
        }

        @Override
        public void apply(HttpRequest request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, ObjectMapper mapper) {
            stream(ctx, consumer);
        }

    }

    private class ViewBody extends AbstractStreamingBody {

        private View view;

        private ViewBody(View view) {
            this.view = view;
        }

        @Override
        public void apply(HttpRequest request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, ObjectMapper mapper) {
            if (viewRenderer.isTemplateAvailable(view)) {
                stream(ctx, (o) -> viewRenderer.render(request, view, o));
            }
            else {
                logger.error("view template [" + view.template + "] is not available");
                internalServerError().apply(request, ctx, viewRenderer, mapper);
            }
        }

    }

    private class JsonBody extends AbstractStreamingBody {

        private Object entity;

        private JsonBody(Object entity) {
            this.entity = entity;
        }

        @Override
        public void apply(HttpRequest request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, ObjectMapper mapper) {
            if (!headers.contains(HttpHeaders.Names.CONTENT_TYPE)) {
                headers.set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            }
            stream(ctx, (o) -> mapper.writeValue(o, entity));
        }

    }

}
