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

import io.netty.handler.codec.http.HttpResponseStatus;
import org.nosceon.titanite.json.JsonMapperLoader;
import org.nosceon.titanite.service.FileService;
import org.nosceon.titanite.service.ResourceService;
import org.nosceon.titanite.view.ViewRendererLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public final class Titanite {

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

    public static Response serveFile(Request request, File file) {
        return FileService.serveFile(request, file);
    }

    public static Function<Request, CompletionStage<Response>> fileService(String directory) {
        return new FileService(new File(directory));
    }

    public static Function<Request, CompletionStage<Response>> fileService(String directory, Function<Request, String> path) {
        return new FileService(new File(directory), path);
    }

    public static Function<Request, CompletionStage<Response>> fileService(File directory) {
        return new FileService(directory);
    }

    public static Function<Request, CompletionStage<Response>> fileService(File directory, Function<Request, String> path) {
        return new FileService(directory, path);
    }

    public static Response serveResource(Request request, String path) {
        return ResourceService.serveResource(request, path);
    }

    public static Function<Request, CompletionStage<Response>> resourceService(String baseResource) {
        return new ResourceService(baseResource);
    }

    public static Function<Request, CompletionStage<Response>> resourceService(String baseResource, Function<Request, String> path) {
        return new ResourceService(baseResource, path);
    }

    public static Function<Request, CompletionStage<Response>> publicResourceService() {
        return new ResourceService(ResourceService.PUBLIC_RESOURCES);
    }

    public static Function<Request, CompletionStage<Response>> publicResourceService(Function<Request, String> path) {
        return new ResourceService(ResourceService.PUBLIC_RESOURCES, path);
    }

    public static Function<Request, CompletionStage<Response>> webJarResourceService() {
        return new ResourceService(ResourceService.WEBJAR_RESOURCES);
    }

    public static Function<Request, CompletionStage<Response>> webJarResourceService(Function<Request, String> path) {
        return new ResourceService(ResourceService.WEBJAR_RESOURCES, path);
    }

    public static final class Responses {

        public static Response status(int status) {
            return new Response(HttpResponseStatus.valueOf(status));
        }

        public static Response ok() {
            return new Response(HttpResponseStatus.OK);
        }

        public static Response created(String location) {
            return new Response(HttpResponseStatus.CREATED).location(location);
        }

        public static Response created(URI location) {
            return new Response(HttpResponseStatus.CREATED).location(location);
        }

        public static Response accepted() {
            return new Response(HttpResponseStatus.ACCEPTED);
        }

        public static Response noContent() {
            return new Response(HttpResponseStatus.NO_CONTENT);
        }

        public static Response seeOther(String location) {
            return new Response(HttpResponseStatus.SEE_OTHER).location(location);
        }

        public static Response seeOther(URI location) {
            return new Response(HttpResponseStatus.SEE_OTHER).location(location);
        }

        public static Response temporaryRedirect(String location) {
            return new Response(HttpResponseStatus.TEMPORARY_REDIRECT).location(location);
        }

        public static Response temporaryRedirect(URI location) {
            return new Response(HttpResponseStatus.TEMPORARY_REDIRECT).location(location);
        }

        public static Response notModified() {
            return new Response(HttpResponseStatus.NOT_MODIFIED);
        }

        public static Response badRequest() {
            return new Response(HttpResponseStatus.BAD_REQUEST);
        }

        public static Response unauthorized() {
            return new Response(HttpResponseStatus.UNAUTHORIZED);
        }

        public static Response forbidden() {
            return new Response(HttpResponseStatus.FORBIDDEN);
        }

        public static Response notFound() {
            return new Response(HttpResponseStatus.NOT_FOUND);
        }

        public static Response notAcceptable() {
            return new Response(HttpResponseStatus.NOT_ACCEPTABLE);
        }

        public static Response methodNotAllowed() {
            return new Response(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }

        public static Response conflict() {
            return new Response(HttpResponseStatus.CONFLICT);
        }

        public static Response unsupportedMediaType() {
            return new Response(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        public static Response notImplemented() {
            return new Response(HttpResponseStatus.NOT_IMPLEMENTED);
        }

    }

}
