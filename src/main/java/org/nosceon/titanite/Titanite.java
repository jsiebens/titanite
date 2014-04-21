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

import org.nosceon.titanite.service.FileService;
import org.nosceon.titanite.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public final class Titanite extends Responses {

    public static final int DEFAULT_IO_WORKER_COUNT = Runtime.getRuntime().availableProcessors() * 2;

    public static final int DEFAULT_MAX_REQUEST_SIZE = 1024 * 1024 * 10;

    public static final Function<Request, Response> WEBJAR_RESOURCES = resourceService("/META-INF/resources/webjars");

    public static final Function<Request, Response> PUBLIC_RESOURCES = resourceService("/public");

    static final Logger LOG = LoggerFactory.getLogger(Titanite.class);

    public static HttpServerConfig config() {
        return new HttpServerConfig();
    }

    public static HttpServer httpServer() {
        return new HttpServer();
    }

    public static HttpServer httpServer(HttpServer.Config config) {
        return new HttpServer(config);
    }

    public static ErrorFilter errors() {
        return new ErrorFilter();
    }

    @SafeVarargs
    public static Function<Request, CompletableFuture<Response>> compose(Function<Request, CompletableFuture<Response>> function, Function<Request, CompletableFuture<Response>>... functions) {
        Chain chain = new Chain(function);
        if (functions != null) {
            for (Function<Request, CompletableFuture<Response>> f : functions) {
                chain = chain.fallbackTo(f);
            }
        }
        return chain;
    }

    public static Function<Request, Response> fileService(String directory) {
        return new FileService(new File(directory));
    }

    public static Function<Request, Response> fileService(File directory) {
        return new FileService(directory);
    }

    public static Function<Request, Response> resourceService(String baseResource) {
        return new ResourceService(baseResource);
    }

    public static Function<Request, CompletableFuture<Response>> sync(Function<Request, Response> f) {
        return (r) -> CompletableFuture.completedFuture(f.apply(r));
    }

    public static Function<Request, CompletableFuture<Response>> async(Function<Request, Response> f) {
        return (r) -> CompletableFuture.supplyAsync(() -> f.apply(r));
    }

    public static Function<Request, CompletableFuture<Response>> async(Function<Request, Response> f, Executor executor) {
        return (r) -> CompletableFuture.supplyAsync(() -> f.apply(r), executor);
    }

    private static final class Chain implements Function<Request, CompletableFuture<Response>> {

        private Function<Request, CompletableFuture<Response>> first;

        private Function<Request, CompletableFuture<Response>> second;

        private Chain(Function<Request, CompletableFuture<Response>> function) {
            this((r) -> notFound().toFuture(), function);
        }

        private Chain(Function<Request, CompletableFuture<Response>> first, Function<Request, CompletableFuture<Response>> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public CompletableFuture<Response> apply(Request request) {
            return
                first.apply(request)
                    .thenCompose(resp -> {
                        if (resp == null || resp.status() == 404) {
                            return second.apply(request);
                        }
                        return CompletableFuture.completedFuture(resp);
                    });
        }

        private Chain fallbackTo(Function<Request, CompletableFuture<Response>> next) {
            return new Chain(this, next);
        }

    }

}
