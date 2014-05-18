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

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import static java.util.Optional.ofNullable;

/**
 * @author Johan Siebens
 */
public final class Request {

    private final HttpMethod method;

    private final String path;

    private final HeaderParams headers;

    private final CookieParams cookies;

    private final QueryParams queryParams;

    private final PathParams pathParams;

    private final RequestBody body;

    Request(HttpMethod method, String path, HeaderParams headers, CookieParams cookies, PathParams pathParams, QueryParams queryParams, RequestBody body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.cookies = cookies;
        this.queryParams = queryParams;
        this.pathParams = pathParams;
        this.body = body;
    }

    public HttpMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    public HeaderParams headers() {
        return headers;
    }

    public CookieParams cookies() {
        return cookies;
    }

    public QueryParams queryParams() {
        return queryParams;
    }

    public PathParams pathParams() {
        return pathParams;
    }

    public RequestBody body() {
        return body;
    }

    public MediaType contentType() {
        return ofNullable(headers.getString(HttpHeaders.Names.CONTENT_TYPE)).map(MediaType::valueOf).orElse(null);
    }

    public String baseUri() {
        return "http://" + headers.getString(HttpHeaders.Names.HOST);
    }

}
