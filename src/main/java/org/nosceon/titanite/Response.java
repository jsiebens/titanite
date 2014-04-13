/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import org.nosceon.titanite.json.JsonMapper;
import org.nosceon.titanite.view.ViewRenderer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.nosceon.titanite.HttpServerException.propagate;
import static org.nosceon.titanite.Responses.internalServerError;

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

    public int status() {
        return status.code();
    }

    public Response header(String name, Object value) {
        headers.add(name, value);
        return this;
    }

    public Response location(String location) {
        headers.set(LOCATION, location);
        return this;
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
        this.body = new DefaultBody(Unpooled.copiedBuffer(content, UTF8));
        return this;
    }

    public Response text(String content) {
        this.type(MediaType.TEXT_PLAIN);
        this.body = new DefaultBody(Unpooled.copiedBuffer(content, UTF8));
        return this;
    }

    public Response html(String content) {
        this.type(MediaType.TEXT_HTML);
        this.body = new DefaultBody(Unpooled.copiedBuffer(content, UTF8));
        return this;
    }

    public Response json(Object entity) {
        this.type(MediaType.APPLICATION_JSON);
        this.body = new JsonBody(entity);
        return this;
    }

    public Response stream(StreamingOutput consumer) {
        this.body = new StreamBody(consumer);
        return this;
    }

    public Response file(File file) {
        this.body = new FileBody(file);
        return this;
    }

    public Response view(Object view) {
        this.body = new ViewBody(view);
        return this;
    }

    public CompletableFuture<Response> toFuture() {
        return completedFuture(this);
    }

    ChannelFuture apply(boolean keepAlive, Request request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, JsonMapper mapper) {
        return body.apply(keepAlive, request, ctx, viewRenderer, mapper);
    }

    private static interface Body {

        ChannelFuture apply(boolean keepAlive, Request request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, JsonMapper mapper);

    }

    private class DefaultBody implements Body {

        private ByteBuf content;

        private DefaultBody(ByteBuf content) {
            this.content = content;
        }

        @Override
        public ChannelFuture apply(boolean keepAlive, Request request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, JsonMapper mapper) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, content);
            response.headers().add(headers);
            setContentLength(response, content.readableBytes());
            setKeepAlive(response, keepAlive);

            return ctx.writeAndFlush(response);
        }

    }

    private abstract class AbstractStreamingBody implements Body {

        protected ChannelFuture stream(ChannelHandlerContext ctx, StreamingOutput streamingOutput) {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
            response.headers().add(headers);
            setTransferEncodingChunked(response);

            ctx.write(response);
            propagate(() -> {
                try (OutputStream out = new ChunkOutputStream(ctx, 1024)) {
                    streamingOutput.apply(out);
                }
                return true;
            });
            return ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }

    }

    private class StreamBody extends AbstractStreamingBody {

        private StreamingOutput consumer;

        private StreamBody(StreamingOutput consumer) {
            this.consumer = consumer;
        }

        @Override
        public ChannelFuture apply(boolean keepAlive, Request request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, JsonMapper mapper) {
            return stream(ctx, consumer);
        }

    }

    private class ViewBody extends AbstractStreamingBody {

        private Object view;

        private ViewBody(Object view) {
            this.view = view;
        }

        @Override
        public ChannelFuture apply(boolean keepAlive, Request request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, JsonMapper mapper) {
            if (viewRenderer.isTemplateAvailable(view)) {
                return stream(ctx, (o) -> viewRenderer.render(request, view, o));
            }
            else {
                return internalServerError().apply(keepAlive, request, ctx, viewRenderer, mapper);
            }
        }

    }

    private class JsonBody extends AbstractStreamingBody {

        private Object entity;

        private JsonBody(Object entity) {
            this.entity = entity;
        }

        @Override
        public ChannelFuture apply(boolean keepAlive, Request request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, JsonMapper mapper) {
            return stream(ctx, (o) -> mapper.write(o, entity));
        }

    }

    private class FileBody implements Body {

        private final File file;

        private FileBody(File file) {
            this.file = file;
        }

        @Override
        public ChannelFuture apply(boolean keepAlive, Request request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, JsonMapper mapper) {

            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
            response.headers().add(headers);

            RandomAccessFile raf;
            long length;
            try {
                raf = new RandomAccessFile(file, "r");
                length = raf.length();
            }
            catch (IOException e) {
                Titanite.LOG.error("error writing file to response", e);
                return internalServerError().apply(keepAlive, request, ctx, viewRenderer, mapper);
            }

            setContentLength(response, length);
            setKeepAlive(response, keepAlive);

            ctx.write(response);
            ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length), ctx.newProgressivePromise());
            return ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }

    }

}
