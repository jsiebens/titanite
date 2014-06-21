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

import com.google.common.base.CharMatcher;
import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;
import org.nosceon.titanite.Titanite;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.jar.JarEntry;

import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.nosceon.titanite.HttpServerException.call;
import static org.nosceon.titanite.Titanite.Responses.*;

/**
 * @author Johan Siebens
 */
public final class ResourceService implements Function<Request, CompletionStage<Response>> {

    private static final CharMatcher SLASH = CharMatcher.is('/');

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
        this.baseResource = SLASH.trimTrailingFrom(baseResource);
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

        return serveResource(request, baseResource + '/' + SLASH.trimLeadingFrom(path));
    }

    public static Response serveResource(Request request, String path) {
        String trimmedPath = SLASH.trimFrom(path);
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

        return notFound();
    }

    private static Response createResponseFromJarResource(Request request, URL resource) {
        Optional<Date> ifModifiedSince = ofNullable(request.headers().getDate(IF_MODIFIED_SINCE));
        long lastModified = lastModificationDateFromJarResource(resource);

        if (lastModified <= 0) {
            return
                ok()
                    .type(MimeTypes.contentType(resource.toString()))
                    .body(call(resource::openStream));
        }
        else {
            return
                ifModifiedSince
                    .filter((d) -> lastModified <= d.getTime())
                    .map((d) -> notModified())
                    .orElseGet(() ->
                        ok()
                            .type(MimeTypes.contentType(resource.toString()))
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

    private static URL getResource(String name) {
        URL url = null;

        ClassLoader ccl = Thread.currentThread().getContextClassLoader();

        if (ccl != null) {
            url = ccl.getResource(name);
        }

        if (url == null) {
            ClassLoader cl = Titanite.class.getClassLoader();
            if (cl != null && cl != ccl) {
                url = cl.getResource(name);
            }
        }

        if (url == null) {
            url = ClassLoader.getSystemResource(name);
        }

        return url;
    }

}
