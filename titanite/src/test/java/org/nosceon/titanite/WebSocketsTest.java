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

import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
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
            .register(Method.GET, "/socket", req -> webSocket(channel -> channel.onMessage(s -> channel.write(s.toUpperCase()))))
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
    public void test() throws Exception {
        CompletableFuture<String> expectedMessage = new CompletableFuture<>();

        Future<org.eclipse.jetty.websocket.WebSocket.Connection> open = client.open(URI.create(ws("/socket")), new org.eclipse.jetty.websocket.WebSocket.OnTextMessage() {

            @Override
            public void onOpen(Connection connection) {
                try {
                    connection.sendMessage("hello world");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(String data) {
                expectedMessage.complete(data);
            }

            @Override
            public void onClose(int closeCode, String message) {

            }

        });

        org.eclipse.jetty.websocket.WebSocket.Connection connection = open.get();

        assertThat(connection, is(notNullValue()));
        assertThat(expectedMessage.get(), equalTo("HELLO WORLD"));

        connection.close();
    }

}
