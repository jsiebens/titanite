package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import static java.util.Optional.ofNullable;

/**
 * @author Johan Siebens
 */
public final class Request {

    public final HttpMethod method;

    public final String path;

    public final HeaderParams headers;

    public final CookieParams cookies;

    public final QueryParams queryParams;

    public final PathParams pathParams;

    public final RequestBody body;

    Request(HttpMethod method, String path, HeaderParams headers, CookieParams cookies, PathParams pathParams, QueryParams queryParams, RequestBody body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.cookies = cookies;
        this.queryParams = queryParams;
        this.pathParams = pathParams;
        this.body = body;
    }

    public MediaType contentType() {
        return ofNullable(headers.get(HttpHeaders.Names.CONTENT_TYPE)).map(MediaType::valueOf).orElse(null);
    }

    public String baseUri() {
        return "http://" + headers.get(HttpHeaders.Names.HOST);
    }

}
