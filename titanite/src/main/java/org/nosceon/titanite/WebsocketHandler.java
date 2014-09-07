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
import io.netty.channel.Channel;
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

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * @author Johan Siebens
 */
class WebsocketHandler {

    private WebSocketServerHandshaker wsHandshaker;

    private WChannel wsChannel;

    public void handshake(HttpRequest rawRequest, Request request, ChannelHandlerContext ctx, WebSocket webSocket) {
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, false);
        wsHandshaker = wsFactory.newHandshaker(rawRequest);
        if (wsHandshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }
        else {
            this.wsChannel = new WChannel(ctx.channel());
            this.wsHandshaker.handshake(ctx.channel(), toFullHttpRequest(rawRequest)).addListener(cf -> {
                ctx.pipeline().addLast(wsChannel);
                webSocket.onReady(wsChannel);
            });
        }
    }

    public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            wsHandshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
        }

        else if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
        }

        else if (frame instanceof BinaryWebSocketFrame) {
            this.wsChannel.publish(toByteArray(frame.content()));
        }

        else if (frame instanceof TextWebSocketFrame) {
            this.wsChannel.publish(((TextWebSocketFrame) frame).text());
        }

        else {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
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

    private static byte[] toByteArray(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    private class WChannel extends ChannelInboundHandlerAdapter implements WebSocket.Channel {

        private List<Consumer<String>> textListeners = new CopyOnWriteArrayList<>();

        private List<Consumer<byte[]>> binaryListeners = new CopyOnWriteArrayList<>();

        private final CompletableFuture<Void> disconnect = new CompletableFuture<>();

        private Channel channel;

        private WChannel(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            disconnect.complete(null);
        }

        @Override
        public void write(String message) {
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }

        @Override
        public void write(byte[] message) {
            channel.writeAndFlush(new BinaryWebSocketFrame(copiedBuffer(message)));
        }

        @Override
        public void onTextMessage(Consumer<String> consumer) {
            textListeners.add(consumer);
        }

        @Override
        public void onBinaryMessage(Consumer<byte[]> consumer) {
            binaryListeners.add(consumer);
        }

        @Override
        public void onClose(Runnable listener) {
            disconnect.whenComplete((v, t) -> listener.run());
        }

        @Override
        public void close() {
            wsHandshaker.close(channel, new CloseWebSocketFrame());
        }

        private void publish(String message) {
            textListeners.forEach(c -> c.accept(message));
        }

        private void publish(byte[] message) {
            binaryListeners.forEach(c -> c.accept(message));
        }

    }

}
