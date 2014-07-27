/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nosceon.titanite;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.io.*;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.nosceon.titanite.Exceptions.internalServerError;

/**
 * @author Johan Siebens
 */
public final class Response {

    public static Response status(int status) {
        return new Response(HttpResponseStatus.valueOf(status));
    }

    public static Response ok() {
        return new Response(HttpResponseStatus.OK);
    }

    public static Response created(String location) {
        return new Response(HttpResponseStatus.CREATED).location(location);
    }

    public static Response created(URI location) {
        return new Response(HttpResponseStatus.CREATED).location(location);
    }

    public static Response accepted() {
        return new Response(HttpResponseStatus.ACCEPTED);
    }

    public static Response noContent() {
        return new Response(HttpResponseStatus.NO_CONTENT);
    }

    public static Response seeOther(String location) {
        return new Response(HttpResponseStatus.SEE_OTHER).location(location);
    }

    public static Response seeOther(URI location) {
        return new Response(HttpResponseStatus.SEE_OTHER).location(location);
    }

    public static Response temporaryRedirect(String location) {
        return new Response(HttpResponseStatus.TEMPORARY_REDIRECT).location(location);
    }

    public static Response temporaryRedirect(URI location) {
        return new Response(HttpResponseStatus.TEMPORARY_REDIRECT).location(location);
    }

    public static Response notModified() {
        return new Response(HttpResponseStatus.NOT_MODIFIED);
    }

    public static Response badRequest() {
        return new Response(HttpResponseStatus.BAD_REQUEST);
    }

    public static Response unauthorized() {
        return new Response(HttpResponseStatus.UNAUTHORIZED);
    }

    public static Response forbidden() {
        return new Response(HttpResponseStatus.FORBIDDEN);
    }

    public static Response notFound() {
        return new Response(HttpResponseStatus.NOT_FOUND);
    }

    public static Response notAcceptable() {
        return new Response(HttpResponseStatus.NOT_ACCEPTABLE);
    }

    public static Response methodNotAllowed() {
        return new Response(HttpResponseStatus.METHOD_NOT_ALLOWED);
    }

    public static Response conflict() {
        return new Response(HttpResponseStatus.CONFLICT);
    }

    public static Response unsupportedMediaType() {
        return new Response(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    public static Response notImplemented() {
        return new Response(HttpResponseStatus.NOT_IMPLEMENTED);
    }

    private HttpResponseStatus status;

    private HttpHeaders headers = new DefaultHttpHeaders();

    private Body body = new NoBody();

    public Response(int status) {
        this(HttpResponseStatus.valueOf(status));
    }

    Response(HttpResponseStatus status) {
        this.status = status;
    }

    public int status() {
        return status.code();
    }

    public Response header(String name, Object value) {
        headers.add(name, value);
        return this;
    }

    public Response location(String location) {
        return location(URI.create(location));
    }

    public Response location(URI location) {
        headers.set(LOCATION, location);
        return this;
    }

    public Response type(MediaType type) {
        headers.set(CONTENT_TYPE, type.toString());
        return this;
    }

    public Response language(Locale language) {
        headers.set(CONTENT_LANGUAGE, language);
        return this;
    }

    public Response lastModified(Date date) {
        headers.set(LAST_MODIFIED, date);
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
        this.body = new DefaultBody(Unpooled.copiedBuffer(content, UTF_8));
        return this;
    }

    public Response body(InputStream in) {
        return body(o -> Utils.copy(in, o));
    }

    public Response body(BodyWriter bodyWriter) {
        this.body = new StreamBody(bodyWriter);
        return this;
    }

    public Response body(File file) {
        this.body = new FileBody(file);
        return this;
    }

    public Response text(String content) {
        this.type(MediaType.TEXT_PLAIN);
        this.body = new DefaultBody(Unpooled.copiedBuffer(content, UTF_8));
        return this;
    }

    public Response html(String content) {
        this.type(MediaType.TEXT_HTML);
        this.body = new DefaultBody(Unpooled.copiedBuffer(content, UTF_8));
        return this;
    }

    public Response chunks(ChunkedOutput chunkedOutput) {
        this.body = new ChunkedBody(chunkedOutput);
        return this;
    }

    public CompletionStage<Response> toFuture() {
        return completedFuture(this);
    }

    void apply(boolean keepAlive, Request request, ChannelHandlerContext ctx) {
        body.apply(keepAlive, request, ctx);
    }

    private static interface Body {

        void apply(boolean keepAlive, Request request, ChannelHandlerContext ctx);

    }

    private static void writeFlushAndClose(ChannelHandlerContext ctx, Object msg, boolean keepAlive) {
        ChannelFuture future = ctx.writeAndFlush(msg);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private class NoBody implements Body {

        @Override
        public void apply(boolean keepAlive, Request request, ChannelHandlerContext ctx) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.EMPTY_BUFFER);
            response.headers().add(headers);
            setKeepAlive(response, keepAlive);
            writeFlushAndClose(ctx, response, false);
        }

    }

    private class DefaultBody implements Body {

        private ByteBuf content;

        private DefaultBody(ByteBuf content) {
            this.content = content;
        }

        @Override
        public void apply(boolean keepAlive, Request request, ChannelHandlerContext ctx) {
            boolean isHeadRequest = request.method().equals(Method.HEAD);

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, isHeadRequest ? Unpooled.EMPTY_BUFFER : content);
            response.headers().add(headers);
            setContentLength(response, content.readableBytes());
            setKeepAlive(response, keepAlive);
            writeFlushAndClose(ctx, response, false);
        }

    }

    private class ChunkedBody implements Body {

        private ChunkedOutput chunkedOutput;

        private ChunkedBody(ChunkedOutput chunkedOutput) {
            this.chunkedOutput = chunkedOutput;
        }

        @Override
        public void apply(boolean keepAlive, Request request, ChannelHandlerContext ctx) {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
            response.headers().add(headers);
            setTransferEncodingChunked(response);

            ctx.writeAndFlush(response);

            if (!request.method().equals(Method.HEAD)) {
                ChunksChannel channel = new ChunksChannel(keepAlive, ctx);
                ctx.pipeline().addLast(channel);

                chunkedOutput.onReady(channel);
            }
            else {
                writeFlushAndClose(ctx, LastHttpContent.EMPTY_LAST_CONTENT, keepAlive);
            }
        }

    }

    private static class ChunksChannel extends ChannelInboundHandlerAdapter implements ChunkedOutput.Channel {

        private final boolean keepAlive;

        private final ChannelHandlerContext ctx;

        private final CompletableFuture<Void> disconnect;

        private ChunksChannel(boolean keepAlive, ChannelHandlerContext ctx) {
            this.keepAlive = keepAlive;
            this.ctx = ctx;
            this.disconnect = new CompletableFuture<>();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            disconnect.complete(null);
        }

        @Override
        public void write(byte[] chunk) {
            ctx.writeAndFlush(new DefaultHttpContent(Unpooled.copiedBuffer(chunk)));
        }

        @Override
        public void close() {
            ctx.pipeline().remove(this);
            writeFlushAndClose(ctx, LastHttpContent.EMPTY_LAST_CONTENT, keepAlive);
        }

        @Override
        public void onDisconnect(Runnable listener) {
            disconnect.whenComplete((v, t) -> listener.run());
        }

    }

    private class StreamBody implements Body {

        private BodyWriter consumer;

        private StreamBody(BodyWriter consumer) {
            this.consumer = consumer;
        }

        @Override
        public void apply(boolean keepAlive, Request request, ChannelHandlerContext ctx) {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
            response.headers().add(headers);
            setTransferEncodingChunked(response);

            ctx.write(response);

            if (!request.method().equals(Method.HEAD)) {
                Utils.runUnchecked(() -> {
                    try (OutputStream out = new ChunkOutputStream(ctx, 1024)) {
                        consumer.writeTo(out);
                    }
                });
            }

            writeFlushAndClose(ctx, LastHttpContent.EMPTY_LAST_CONTENT, keepAlive);
        }

    }

    private class FileBody implements Body {

        private final File file;

        private FileBody(File file) {
            this.file = file;
        }

        @Override
        public void apply(boolean keepAlive, Request request, ChannelHandlerContext ctx) {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
            response.headers().add(headers);

            try {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                long length = raf.length();

                setContentLength(response, length);
                setKeepAlive(response, keepAlive);

                ctx.write(response);

                if (!request.method().equals(Method.HEAD)) {
                    ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length), ctx.newProgressivePromise());
                }

                writeFlushAndClose(ctx, LastHttpContent.EMPTY_LAST_CONTENT, keepAlive);
            }
            catch (IOException e) {
                Titanite.LOG.error("error writing file to response", e);
                internalServerError().apply(keepAlive, request, ctx);
            }
        }

    }

}
