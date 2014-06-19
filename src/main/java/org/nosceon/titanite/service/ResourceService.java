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
package org.nosceon.titanite.service;

import org.eclipse.jetty.util.resource.Resource;
import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;
import org.nosceon.titanite.Titanite;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.eclipse.jetty.util.resource.Resource.newClassPathResource;
import static org.nosceon.titanite.HttpServerException.call;
import static org.nosceon.titanite.Titanite.Responses.*;

/**
 * @author Johan Siebens
 */
public class ResourceService implements Function<Request, CompletionStage<Response>> {

    public static final String WEBJAR_RESOURCES = "/META-INF/resources/webjars";

    public static final String PUBLIC_RESOURCES = "/public";

    private final String baseResource;

    private final Executor executor;

    private final Function<Request, String> pathExtractor;

    public ResourceService(String baseResource) {
        this(baseResource, Request::path);
    }

    public ResourceService(String baseResource, Function<Request, String> pathExtractor) {
        this(baseResource, pathExtractor, Runnable::run);
    }

    public ResourceService(String baseResource, Function<Request, String> pathExtractor, Executor executor) {
        this.baseResource = baseResource;
        this.pathExtractor = pathExtractor;
        this.executor = executor;
    }

    @Override
    public final CompletionStage<Response> apply(Request request) {
        return supplyAsync(() -> internalApply(request), executor);
    }

    private Response internalApply(Request request) {
        String path = Optional.ofNullable(pathExtractor.apply(request)).get();

        if (path.contains("..")) {
            return forbidden();
        }

        return
            ofNullable(newClassPathResource(baseResource + '/' + path))
                .filter((r) -> r.exists() && !r.isDirectory())
                .map((r) -> createResponse(request, r))
                .orElseGet(Titanite.Responses::notFound);
    }

    private Response createResponse(Request request, Resource resource) {
        Optional<Date> ifModifiedSince = ofNullable(request.headers().getDate(IF_MODIFIED_SINCE));
        long lastModified = resource.lastModified();

        if (lastModified <= 0) {
            return
                ok()
                    .type(MimeTypes.contentType(resource.getName()))
                    .body(call(resource::getInputStream));
        }
        else {
            return
                ifModifiedSince
                    .filter((d) -> lastModified <= d.getTime())
                    .map((d) -> notModified())
                    .orElseGet(() ->
                        ok()
                            .type(MimeTypes.contentType(resource.getName()))
                            .lastModified(new Date(lastModified))
                            .body(call(resource::getInputStream)));
        }
    }

}
