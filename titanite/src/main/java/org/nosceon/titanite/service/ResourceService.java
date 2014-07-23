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

import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;
import org.nosceon.titanite.Utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.jar.JarEntry;

import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.HttpServerException.call;
import static org.nosceon.titanite.Utils.getMediaTypeFromFileName;
import static org.nosceon.titanite.Utils.getResource;

/**
 * @author Johan Siebens
 */
public final class ResourceService implements Function<Request, CompletionStage<Response>> {

    public static Function<Request, CompletionStage<Response>> resourceService(String baseResource) {
        return new ResourceService(baseResource);
    }

    public static Function<Request, CompletionStage<Response>> resourceService(String baseResource, Function<Request, String> path) {
        return new ResourceService(baseResource, path);
    }

    public static Function<Request, CompletionStage<Response>> publicResourceService() {
        return new ResourceService(PUBLIC_RESOURCES);
    }

    public static Function<Request, CompletionStage<Response>> publicResourceService(Function<Request, String> path) {
        return new ResourceService(PUBLIC_RESOURCES, path);
    }

    public static Function<Request, CompletionStage<Response>> webJarResourceService() {
        return new ResourceService(WEBJAR_RESOURCES);
    }

    public static Function<Request, CompletionStage<Response>> webJarResourceService(Function<Request, String> path) {
        return new ResourceService(WEBJAR_RESOURCES, path);
    }

    public static Response serveResource(Request request, String path) {
        String trimmedPath = Utils.trim(SLASH, path);
        URL url = getResource(trimmedPath);

        if (url != null) {
            String protocol = url.getProtocol();
            switch (protocol) {
                case "file":
                    return FileService.serveFile(request, new File(url.getFile()));
                case "jar":
                    // http://stackoverflow.com/a/20107785
                    if (getResource(trimmedPath + '/') == null) {
                        return createResponseFromJarResource(request, url);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported protocol " + url.getProtocol() + " for resource " + url);
            }
        }

        return Response.notFound();
    }

    private static final char SLASH = '/';

    public static final String WEBJAR_RESOURCES = "/META-INF/resources/webjars";

    public static final String PUBLIC_RESOURCES = "/public";

    private final String baseResource;

    private final Function<Request, String> pathExtractor;

    public ResourceService(String baseResource) {
        this(baseResource, Request::path);
    }

    public ResourceService(String baseResource, Function<Request, String> pathExtractor) {
        this.baseResource = Utils.trimTrailing(SLASH, baseResource);
        this.pathExtractor = pathExtractor;
    }

    @Override
    public final CompletionStage<Response> apply(Request request) {
        String path = Optional.ofNullable(pathExtractor.apply(request)).get();

        if (path.contains("..")) {
            return Response.forbidden().toFuture();
        }

        return serveResource(request, baseResource + SLASH + Utils.trimLeading(SLASH, path)).toFuture();
    }

    private static Response createResponseFromJarResource(Request request, URL resource) {
        Optional<Date> ifModifiedSince = ofNullable(request.headers().getDate(IF_MODIFIED_SINCE));
        long lastModified = lastModificationDateFromJarResource(resource);

        if (lastModified <= 0) {
            return
                Response.ok()
                    .type(getMediaTypeFromFileName(resource.toString()))
                    .body(call(resource::openStream));
        }
        else {
            return
                ifModifiedSince
                    .filter((d) -> lastModified <= d.getTime())
                    .map((d) -> Response.notModified())
                    .orElseGet(() ->
                        Response.ok()
                            .type(getMediaTypeFromFileName(resource.toString()))
                            .lastModified(new Date(lastModified))
                            .body(call(resource::openStream)));
        }
    }

    private static long lastModificationDateFromJarResource(URL url) {
        try {
            final JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
            final JarEntry entry = jarConnection.getJarEntry();
            return entry.getTime();
        }
        catch (IOException ignored) {
            return 0;
        }
    }


}
