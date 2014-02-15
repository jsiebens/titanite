package org.nosceon.titanite;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.OutputStream;
import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.nosceon.titanite.HttpServerException.propagate;

/**
 * @author Johan Siebens
 */
public final class Response {

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

    public Response body(String content) {
        this.body = new DefaultBody(Unpooled.copiedBuffer(content, UTF8));
        return this;
    }

    public Response body(StreamingOutput consumer) {
        this.body = new StreamBody(consumer);
        return this;
    }

    void apply(HttpRequest request, ChannelHandlerContext ctx) {
        body.apply(request, ctx);
    }

    private static interface Body {

        void apply(HttpRequest request, ChannelHandlerContext ctx);

    }

    private class DefaultBody implements Body {

        private ByteBuf content;

        private DefaultBody(ByteBuf content) {
            this.content = content;
        }

        @Override
        public void apply(HttpRequest request, ChannelHandlerContext ctx) {
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

    private class StreamBody implements Body {

        private StreamingOutput consumer;

        private StreamBody(StreamingOutput consumer) {
            this.consumer = consumer;
        }

        @Override
        public void apply(HttpRequest request, ChannelHandlerContext ctx) {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
            response.headers().add(headers);
            HttpHeaders.setTransferEncodingChunked(response);
            ctx.write(response);
            propagate(() -> {
                try (OutputStream out = new ChunkOutputStream(ctx, 1024)) {
                    consumer.apply(out);
                }
                return true;
            });
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
        }

    }

}
