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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.nosceon.titanite.body.BodyParser;
import org.nosceon.titanite.body.EmptyBodyParser;
import org.nosceon.titanite.body.FormParamsBodyParser;
import org.nosceon.titanite.body.RawBodyParser;

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

/**
 * @author Johan Siebens
 */
final class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final FullHttpResponse CONTINUE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);

    private final Router router;

    private HttpRequest request;

    private QueryStringDecoder qsd;

    private RoutingResult routing;

    private BodyParser bodyParser;

    private final long maxRequestSize;

    private final long maxMultipartRequestSize;

    private final WebsocketHandler websocketHandler = new WebsocketHandler();

    private final boolean secure;

    public HttpServerHandler(boolean secure, long maxRequestSize, long maxMultipartRequestSize, Router router) {
        this.secure = secure;
        this.router = router;
        this.maxRequestSize = maxRequestSize;
        this.maxMultipartRequestSize = maxMultipartRequestSize;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            websocketHandler.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            return;
        }

        if (msg instanceof HttpRequest) {
            if (is100ContinueExpected((HttpRequest) msg)) {
                ctx.writeAndFlush(CONTINUE).addListener(future -> {
                    if (!future.isSuccess()) {
                        ctx.fireExceptionCaught(future.cause());
                    }
                });
            }

            this.request = (HttpRequest) msg;
            this.qsd = new QueryStringDecoder(request.getUri());
            this.routing = router.find(request.getMethod(), qsd.path());
            this.bodyParser = newBodyParser(routing, request);
            this.bodyParser.initialize(ctx, request);
        }

        if (msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;

            if (bodyParser != null) {
                bodyParser.offer(chunk);
            }

            if (chunk instanceof LastHttpContent) {

                Map<String, CookieParam> cookies = Optional.ofNullable(request.headers().get(COOKIE))
                    .map(CookieDecoder::decode)
                    .map(s -> s.stream().collect(toMap(io.netty.handler.codec.http.Cookie::getName, CookieParam::new)))
                    .orElseGet(Collections::emptyMap);

                request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bodyParser.size());

                Request req =
                    new Request(
                        secure,
                        Method.valueOf(request.getMethod().name()),
                        qsd.path(),
                        new HeaderParams(request),
                        new CookieParams(cookies),
                        new PathParams(routing.pathParams()),
                        new QueryParams(qsd.parameters()),
                        bodyParser.body()
                    );

                if (bodyParser.isMaximumExceeded()) {
                    requestEntityTooLarge().apply(request, websocketHandler, isKeepAlive(request), req, ctx);
                }
                else {
                    completedFuture(req)
                        .thenCompose(r -> routing.handler().apply(r))
                        .whenComplete((resp, ex) -> {
                            releaseBodyParser();
                            Response response = resp;
                            if (ex != null) {
                                if (ex instanceof CompletionException) {
                                    ex = lookupCause(ex);
                                }

                                if (ex instanceof InternalRuntimeException) {
                                    ex = lookupCause(ex);
                                }

                                if (ex instanceof HttpServerException) {
                                    response = ((HttpServerException) ex).getResponse();

                                    if (response.status() >= 500) {
                                        Titanite.LOG.error("error processing request", ex);
                                    }
                                }
                                else {
                                    Titanite.LOG.error("error processing request", ex);
                                    response = internalServerError();
                                }
                            }
                            response.apply(request, websocketHandler, isKeepAlive(request), req, ctx);
                        });
                }

            }
        }

    }

    private Throwable lookupCause(Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            return cause;
        }
        return e;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        releaseBodyParser();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Titanite.LOG.warn("error handling request", cause);
        releaseBodyParser();
        ctx.channel().close();
    }

    private void releaseBodyParser() {
        if (bodyParser != null) {
            bodyParser.release();
            bodyParser = null;
        }
    }

    private BodyParser newBodyParser(RoutingResult routing, HttpRequest request) {
        HttpMethod method = request.getMethod();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) || method.equals(HttpMethod.PATCH)) {

            if (routing.bodyParser() != null) {
                BodyParser bp = routing.bodyParser().get();
                if (bp != null) {
                    return bp;
                }
            }

            String contentType = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);

            if (contentType != null) {
                String lowerCaseContentType = contentType.toLowerCase();
                boolean isURLEncoded = lowerCaseContentType.startsWith(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);
                boolean isMultiPart = lowerCaseContentType.startsWith(HttpHeaders.Values.MULTIPART_FORM_DATA);

                if (isMultiPart) {
                    return new FormParamsBodyParser(maxMultipartRequestSize);
                }

                if (isURLEncoded) {
                    return new FormParamsBodyParser(maxRequestSize);
                }
            }

            return new RawBodyParser(maxRequestSize);
        }
        else {
            return new EmptyBodyParser();
        }
    }

}
