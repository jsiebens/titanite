package org.nosceon.titanite;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;

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

    public Response view(View view) {
        this.body = new ViewBody(view);
        return this;
    }

    public CompletableFuture<Response> toFuture() {
        return completedFuture(this);
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
            setContentLength(response, content.readableBytes());
            setKeepAlive(response, keepAlive);

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
            setTransferEncodingChunked(response);

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
                Titanite.LOG.error("view template [" + view.template + "] is not available");
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
            stream(ctx, (o) -> mapper.writeValue(o, entity));
        }

    }

    private class FileBody implements Body {

        private final File file;

        private FileBody(File file) {
            this.file = file;
        }

        @Override
        public void apply(HttpRequest request, ChannelHandlerContext ctx, ViewRenderer viewRenderer, ObjectMapper mapper) {
            boolean keepAlive = isKeepAlive(request);

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
                internalServerError().apply(request, ctx, viewRenderer, mapper);
                return;
            }

            setContentLength(response, length);
            setKeepAlive(response, keepAlive);

            ctx.write(response);
            ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length), ctx.newProgressivePromise());
            ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

            if (!keepAlive) {
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        }

    }

}
