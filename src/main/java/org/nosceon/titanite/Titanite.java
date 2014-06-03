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
import org.eclipse.jetty.util.resource.Resource;
import org.nosceon.titanite.json.JsonMapperLoader;
import org.nosceon.titanite.view.ViewRendererLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static java.util.Optional.ofNullable;
import static org.eclipse.jetty.util.resource.Resource.newClassPathResource;
import static org.nosceon.titanite.HttpServerException.call;
import static org.nosceon.titanite.Titanite.Responses.*;

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

    public static final class Assets {

        public static Response sendFile(Request request, File base) {
            return sendFile(request, base, request.path());
        }

        public static Response sendFile(Request request, File base, String path) {
            if (path.contains("..")) {
                return forbidden();
            }

            File file = new File(base, path);

            if (!file.exists() || !file.canRead() || file.isHidden() || file.isDirectory()) {
                return notFound();
            }

            Optional<Date> ifModifiedSince = ofNullable(request.headers().getDate(IF_MODIFIED_SINCE));
            long lastModified = file.lastModified();

            if (lastModified <= 0) {
                return
                    ok()
                        .type(MimeTypes.contentType(file.getName()))
                        .body(file);
            }
            else {
                return
                    ifModifiedSince
                        .filter((d) -> lastModified <= d.getTime())
                        .map((d) -> notModified())
                        .orElseGet(() ->
                            ok()
                                .type(MimeTypes.contentType(file.getName()))
                                .lastModified(new Date(lastModified))
                                .body(file));
            }
        }

        public static Response sendResource(Request request, String base) {
            return sendResource(request, base, request.path());
        }

        public static Response sendResource(Request request, String base, String path) {
            if (path.contains("..")) {
                return forbidden();
            }

            Resource resource = newClassPathResource(base).getResource(path);

            if (!resource.exists() || resource.isDirectory()) {
                return notFound();
            }

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

        public static Response sendPublicResource(Request request) {
            return sendPublicResource(request, request.path());
        }

        public static Response sendPublicResource(Request request, String path) {
            return sendResource(request, "/public", path);
        }

        public static Response sendWebJarResource(Request request) {
            return sendWebJarResource(request, request.path());
        }

        public static Response sendWebJarResource(Request request, String path) {
            return sendResource(request, "/META-INF/resources/webjars", path);
        }

    }

    public static final class JsonSupport {

        public static <T> BodyReader<T> json(Class<T> type) {
            return JsonMapperLoader.get().in(type);
        }

        public static BodyWriter json(Object value) {
            return JsonMapperLoader.get().out(value);
        }

    }

    public static final class ViewSupport {

        public static BodyWriter render(Object view) {
            return ViewRendererLoader.get().apply(view);
        }

    }

}
