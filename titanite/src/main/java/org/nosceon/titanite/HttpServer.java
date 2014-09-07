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

import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Johan Siebens
 */
public final class HttpServer extends AbstractHttpServerBuilder<HttpServer> {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final HttpServerConfig config;

    public HttpServer() {
        this(new DefaultHttpServerConfig());
    }

    public HttpServer(HttpServerConfig config) {
        super("Http Server [" + Utils.padStart(String.valueOf(COUNTER.incrementAndGet()), 3, '0') + "]");
        this.config = config;
    }

    public static HttpServer httpServer() {
        return new HttpServer();
    }

    public static HttpServer httpServer(HttpServerConfig config) {
        return new HttpServer(config);
    }

    public Shutdownable start() {
        Titanite.LOG.info(id + " starting");

        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(config.getIoWorkerCount(), new NamedThreadFactory("Titanite " + id + " - "));
        start(eventLoopGroup, config);

        Titanite.LOG.info(id + " started, listening on port " + config.getPort());

        return eventLoopGroup::shutdownGracefully;
    }

    @Override
    protected HttpServer self() {
        return this;
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final AtomicLong counter = new AtomicLong();

        private final String prefix;

        public NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, prefix + counter.incrementAndGet());
        }

    }

}
