package org.nosceon.titanite;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Johan Siebens
 */
public final class Response {

    public static final Charset UTF8 = Charset.forName("UTF8");

    public static Response status(int status) {
        return new Response(HttpResponseStatus.valueOf(status));
    }

    public static Response status(int status, String content) {
        return status(status).body(content);
    }

    public static Response ok() {
        return new Response(HttpResponseStatus.OK);
    }

    public static Response ok(String content) {
        return ok().body(content);
    }

    public static Response notFound() {
        return new Response(HttpResponseStatus.NOT_FOUND);
    }

    public static Response methodNotAllowed() {
        return new Response(HttpResponseStatus.METHOD_NOT_ALLOWED);
    }

    public static Response internalServerError() {
        return new Response(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpResponseStatus status;

    private HttpHeaders headers = new DefaultHttpHeaders();

    private ByteBuf body;

    private Response(HttpResponseStatus status) {
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
        this.body = Unpooled.copiedBuffer(content, UTF8);
        return this;
    }

    void apply(HttpRequest request, ChannelHandlerContext ctx) {
        ByteBuf content = Optional.ofNullable(body).orElseGet(() -> Unpooled.EMPTY_BUFFER);
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
