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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Johan Siebens
 */
public final class HttpServer extends AbstractHttpServerBuilder<HttpServer> {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;

    private int maxRequestSize = 1024 * 1024 * 10;

    public HttpServer() {
    }

    public HttpServer(int ioWorkerCount) {
        this.ioWorkerCount = ioWorkerCount;
    }

    public HttpServer(int ioWorkerCount, int maxRequestSize) {
        this.ioWorkerCount = ioWorkerCount;
        this.maxRequestSize = maxRequestSize;
    }

    public Shutdownable start(int port) {
        String id = Strings.padStart(String.valueOf(COUNTER.incrementAndGet()), 3, '0');

        Titanite.LOG.info("Http Server [" + id + "] starting");

        Router router = router(id);
        ViewRenderer renderer = new ViewRenderer();
        ObjectMapper mapper = mapper();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(ioWorkerCount, new NamedThreadFactory("Titanite HttpServer [" + id + "] - "));

        newHttpServerBootstrap(eventLoopGroup, maxRequestSize, router, renderer, mapper).bind(port).syncUninterruptibly();

        Titanite.LOG.info("Http Server [" + id + "] started, listening on port " + port);

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
