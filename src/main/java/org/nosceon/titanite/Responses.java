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

import io.netty.handler.codec.http.HttpResponseStatus;
import org.nosceon.titanite.json.JsonMapperLoader;
import org.nosceon.titanite.view.ViewRendererLoader;

import java.net.URI;

/**
 * @author Johan Siebens
 */
public abstract class Responses {

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


    // body

    public static <T> StreamingInput<T> json(Class<T> type) {
        return JsonMapperLoader.get().in(type);
    }

    public static StreamingOutput view(Object view) {
        return ViewRendererLoader.get().apply(view);
    }

    public static StreamingOutput json(Object value) {
        return JsonMapperLoader.get().out(value);
    }


}
