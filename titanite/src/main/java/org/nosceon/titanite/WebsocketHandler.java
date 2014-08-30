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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author Johan Siebens
 */
public class WebsocketHandler {

    private WebSocketServerHandshaker handshaker;

    private WChannel channel;

    public void handshake(HttpRequest rawRequest, Request request, ChannelHandlerContext ctx, WebSocket webSocket) {
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, false);
        handshaker = wsFactory.newHandshaker(rawRequest);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }
        else {
            this.channel = new WChannel(ctx);
            this.handshaker.handshake(ctx.channel(), toFullHttpRequest(rawRequest)).addListener(cf -> {
                ctx.pipeline().addLast(channel);
                webSocket.onReady(channel);
            });
        }
    }

    public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                .getName()));
        }

        this.channel.publish(((TextWebSocketFrame) frame).text());
    }

    private static String getWebSocketLocation(Request req) {
        String location = req.headers().getString(HttpHeaders.Names.HOST) + req.path();
        return "ws://" + location;
    }

    private static FullHttpRequest toFullHttpRequest(HttpRequest request) {
        DefaultFullHttpRequest fullRequest = new DefaultFullHttpRequest(request.getProtocolVersion(), request.getMethod(), request.getUri());
        fullRequest.headers().set(request.headers());
        return fullRequest;
    }

    private static class WChannel extends ChannelInboundHandlerAdapter implements WebSocket.Channel {

        private List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

        private final CompletableFuture<Void> disconnect = new CompletableFuture<>();

        private ChannelHandlerContext ctx;

        private WChannel(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            disconnect.complete(null);
        }

        @Override
        public void write(String message) {
            ctx.writeAndFlush(new TextWebSocketFrame(message));
        }

        @Override
        public void onMessage(Consumer<String> consumer) {
            listeners.add(consumer);
        }

        @Override
        public void onDisconnect(Runnable listener) {
            disconnect.whenComplete((v, t) -> listener.run());
        }

        private void publish(String message) {
            listeners.forEach(c -> c.accept(message));
        }

        private void close() {
            ctx.pipeline().remove(this);
            disconnect.complete(null);
        }

    }

}
