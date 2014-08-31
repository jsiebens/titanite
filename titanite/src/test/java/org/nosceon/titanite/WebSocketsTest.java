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

import org.eclipse.jetty.websocket.*;
import org.eclipse.jetty.websocket.WebSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.nosceon.titanite.Response.webSocket;

/**
 * @author Johan Siebens
 */
public class WebSocketsTest extends AbstractE2ETest {

    private WebSocketClientFactory factory = new WebSocketClientFactory();

    private WebSocketClient client;

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return server
            .register(Method.GET, "/socket", req -> webSocket(channel -> {
                channel.onTextMessage(s -> channel.write(s.toUpperCase()));
                channel.onBinaryMessage(b -> {
                    String s = new String(b);
                    channel.write(s.toLowerCase().getBytes());
                });
            }))
            .start();
    }

    @Before
    public void setUp() throws Exception {
        factory.start();
        this.client = factory.newWebSocketClient();
    }

    @After
    public void tearDown() throws Exception {
        factory.stop();
    }

    @Test(timeout = 2000)
    public void testText() throws Exception {
        CompletableFuture<String> expectedMessage = new CompletableFuture<>();

        Future<org.eclipse.jetty.websocket.WebSocket.Connection> open = client.open(URI.create(ws("/socket")), new org.eclipse.jetty.websocket.WebSocket.OnTextMessage() {

            private int count = 0;

            @Override
            public void onOpen(Connection connection) {
                try {
                    connection.sendMessage("hello world 1");
                    connection.sendMessage("hello world 2");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(String data) {
                count++;
                if (count == 2) {
                    expectedMessage.complete(data);
                }
            }

            @Override
            public void onClose(int closeCode, String message) {

            }

        });

        org.eclipse.jetty.websocket.WebSocket.Connection connection = open.get();

        assertThat(connection, is(notNullValue()));
        assertThat(expectedMessage.get(), equalTo("HELLO WORLD 2"));

        connection.close();
    }

    @Test(timeout = 2000)
    public void testBinary() throws Exception {
        CompletableFuture<byte[]> expectedMessage = new CompletableFuture<>();

        Future<org.eclipse.jetty.websocket.WebSocket.Connection> open = client.open(URI.create(ws("/socket")), new WebSocket.OnBinaryMessage() {

            @Override
            public void onOpen(Connection connection) {
                try {
                    byte[] data = "HELLO WORLD".getBytes();
                    connection.sendMessage(data, 0, data.length);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(byte[] data, int offset, int length) {
                byte[] bytes = Arrays.copyOfRange(data, offset, offset + length);
                expectedMessage.complete(bytes);
            }

            @Override
            public void onClose(int closeCode, String message) {

            }

        });

        org.eclipse.jetty.websocket.WebSocket.Connection connection = open.get();

        assertThat(connection, is(notNullValue()));
        assertThat(expectedMessage.get(), equalTo("hello world".getBytes()));

        connection.close();
    }

}
