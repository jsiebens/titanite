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

import com.google.common.io.CharStreams;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.nosceon.titanite.json.JsonMapper;
import org.nosceon.titanite.view.ViewRenderer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toMap;
import static org.nosceon.titanite.Exceptions.internalServerError;
import static org.nosceon.titanite.Exceptions.requestEntityTooLarge;
import static org.nosceon.titanite.HttpServerException.propagate;

/**
 * @author Johan Siebens
 */
final class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final FullHttpResponse CONTINUE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);

    private final Router router;

    private final ViewRenderer renderer;

    private final JsonMapper mapper;

    private HttpRequest request;

    private Aggregator aggregator;

    private final long maxRequestSize;

    private long currentRequestSize;

    public HttpServerHandler(long maxRequestSize, Router router, ViewRenderer renderer, JsonMapper mapper) {
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
            this.currentRequestSize = 0;

            if (is100ContinueExpected(request)) {
                ctx.writeAndFlush(CONTINUE).addListener(future -> {
                    if (!future.isSuccess()) {
                        ctx.fireExceptionCaught(future.cause());
                    }
                });
            }
        }

        if (msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;
            int chunkSize = chunk.content().readableBytes();

            if (currentRequestSize > maxRequestSize - chunkSize) {
                releaseAggregator();
            }

            if (aggregator != null) {
                aggregator.offer(chunk);
            }

            currentRequestSize += chunkSize;

            if (chunk instanceof LastHttpContent) {
                QueryStringDecoder qsd = new QueryStringDecoder(request.getUri());
                RoutingResult routing = router.find(request.getMethod(), qsd.path());

                Map<String, CookieParam> cookies = Optional.ofNullable(request.headers().get(COOKIE))
                    .map(CookieDecoder::decode)
                    .map(s -> s.stream().collect(toMap(io.netty.handler.codec.http.Cookie::getName, CookieParam::new)))
                    .orElseGet(Collections::emptyMap);

                request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, currentRequestSize);

                Request req =
                    new Request(
                        request.getMethod(),
                        qsd.path(),
                        new HeaderParams(request),
                        new CookieParams(cookies),
                        new PathParams(routing.pathParams),
                        new QueryParams(qsd.parameters()),
                        aggregator == null ? new ExceededSizeRequestBody() : aggregator.newRequestBody()
                    );


                completedFuture(req)
                    .<Response>thenCompose(routing.function::apply)
                    .whenComplete((r, e) -> {
                        releaseAggregator();
                        Response response = r;
                        if (e != null) {
                            if (e instanceof CompletionException) {
                                e = lookupCause((CompletionException) e);
                            }

                            if (e instanceof HttpServerException) {
                                Titanite.LOG.error("error processing request", e);
                                response = ((HttpServerException) e).getResponse();
                            }
                            else {
                                Titanite.LOG.error("error processing request", e);
                                response = internalServerError();
                            }
                        }
                        response.apply(isKeepAlive(request), req, ctx, renderer, mapper);
                    });

            }
        }

    }

    private Throwable lookupCause(CompletionException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            return cause;
        }
        return e;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        releaseAggregator();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Titanite.LOG.warn("error handling request", cause);
        releaseAggregator();
        ctx.channel().close();
    }

    private void releaseAggregator() {
        if (aggregator != null) {
            aggregator.release();
            aggregator = null;
        }
    }

    private Aggregator newAggregator(HttpRequest request, ChannelHandlerContext ctx) {
        HttpMethod method = request.getMethod();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) || method.equals(HttpMethod.PATCH)) {
            String contentType = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);
            if (contentType != null) {
                String lowerCaseContentType = contentType.toLowerCase();
                boolean isURLEncoded = lowerCaseContentType.startsWith(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);
                boolean isMultiPart = lowerCaseContentType.startsWith(HttpHeaders.Values.MULTIPART_FORM_DATA);

                if (isURLEncoded || isMultiPart) {
                    return new FormAggregator(new HttpPostRequestDecoder(request));
                }
            }
            return new DefaultAggregator(ctx.alloc().compositeBuffer());
        }
        else {
            return new NoOpAggregator();
        }
    }

    private static interface Aggregator {

        void offer(HttpContent chunk);

        void release();

        RequestBody newRequestBody();

    }

    private class DefaultAggregator implements Aggregator {

        private CompositeByteBuf content;

        private DefaultAggregator(CompositeByteBuf content) {
            this.content = content;
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

    private class FormAggregator implements Aggregator {

        private HttpPostRequestDecoder decoder;

        private FormAggregator(HttpPostRequestDecoder decoder) {
            this.decoder = decoder;
        }

        @Override
        public void offer(HttpContent chunk) {
            decoder.offer(chunk);
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

    private class NoOpAggregator implements Aggregator {

        @Override
        public void offer(HttpContent chunk) {
        }

        @Override
        public void release() {
        }

        @Override
        public RequestBody newRequestBody() {
            return new DefaultRequestBody(Unpooled.EMPTY_BUFFER);
        }

    }

    private class DefaultRequestBody implements RequestBody {

        private ByteBuf content;

        private DefaultRequestBody(ByteBuf content) {
            this.content = content;
        }

        @Override
        public InputStream asStream() {
            return new ByteBufInputStream(content);
        }

        @Override
        public String asText() {
            return propagate(() -> {
                try (Reader in = new InputStreamReader(asStream())) {
                    return CharStreams.toString(in);
                }
            });
        }

        @Override
        public <T> T asJson(Class<T> type) {
            if (content.readableBytes() > 0) {
                return mapper.read(asStream(), type);
            }
            else {
                return null;
            }
        }

        @Override
        public FormParams asForm() {
            throw new UnsupportedOperationException("asForm not supported");
        }

    }

    private class FormRequestBody implements RequestBody {

        private FormParams form;

        private FormRequestBody(HttpPostRequestDecoder decoder) {
            this.form = new FormParams(decoder);
        }

        @Override
        public InputStream asStream() {
            throw new UnsupportedOperationException("asStream not supported");
        }

        @Override
        public String asText() {
            throw new UnsupportedOperationException("asText not supported");
        }

        @Override
        public <T> T asJson(Class<T> type) {
            throw new UnsupportedOperationException("asJson not supported");
        }

        @Override
        public FormParams asForm() {
            return form;
        }

    }

    private class ExceededSizeRequestBody implements RequestBody {

        @Override
        public boolean maxRequestSizeExceeded() {
            return true;
        }

        @Override
        public InputStream asStream() {
            throw new HttpServerException(requestEntityTooLarge());
        }

        @Override
        public String asText() {
            throw new HttpServerException(requestEntityTooLarge());
        }

        @Override
        public <T> T asJson(Class<T> type) {
            throw new HttpServerException(requestEntityTooLarge());
        }

        @Override
        public FormParams asForm() {
            throw new HttpServerException(requestEntityTooLarge());
        }

    }

}
