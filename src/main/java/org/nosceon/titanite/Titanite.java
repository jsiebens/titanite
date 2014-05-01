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
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public final class Titanite extends Responses {

    public static final int DEFAULT_PORT = 8080;

    public static final int DEFAULT_IO_WORKER_COUNT = Runtime.getRuntime().availableProcessors() * 2;

    public static final int DEFAULT_MAX_REQUEST_SIZE = 1024 * 1024 * 10;

    public static final Function<Request, CompletableFuture<Response>> WEBJAR_RESOURCES = resourceService("/META-INF/resources/webjars");

    public static final Function<Request, CompletableFuture<Response>> PUBLIC_RESOURCES = resourceService("/public");

    static final Logger LOG = LoggerFactory.getLogger(Titanite.class);

    public static HttpServerConfig.Default config() {
        return new HttpServerConfig.Default();
    }

    public static HttpServer httpServer() {
        return new HttpServer();
    }

    public static HttpServer httpServer(HttpServerConfig config) {
        return new HttpServer(config);
    }

    public static ErrorFilter errors() {
        return new ErrorFilter();
    }

    public static Function<Request, CompletableFuture<Response>> fileService(String directory) {
        return new FileService(new File(directory));
    }

    public static Function<Request, CompletableFuture<Response>> fileService(File directory) {
        return new FileService(directory);
    }

    public static Function<Request, CompletableFuture<Response>> resourceService(String baseResource) {
        return new ResourceService(baseResource);
    }

}
