package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;

/**
 * @author Johan Siebens
 */
public final class Request {

    public final HttpMethod method;

    public final String path;

    public final Params headers;

    public final Params queryParams;

    public final Params pathParams;

    public final RequestBody body;

    public Request(HttpMethod method, String path, Params headers, Params pathParams, Params queryParams, RequestBody body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.queryParams = queryParams;
        this.pathParams = pathParams;
        this.body = body;
    }

}
