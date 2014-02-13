package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Johan Siebens
 */
public abstract class Responses {

    public static Response status(int status) {
        return new Response(HttpResponseStatus.valueOf(status));
    }

    public static Response status(int status, String content) {
        return status(status).body(content);
    }

    public static Response ok() {
        return new Response(HttpResponseStatus.OK);
    }

    public static Response ok(String content) {
        return ok().body(content);
    }

    public static Response notFound() {
        return new Response(HttpResponseStatus.NOT_FOUND);
    }

    public static Response methodNotAllowed() {
        return new Response(HttpResponseStatus.METHOD_NOT_ALLOWED);
    }

    public static Response internalServerError() {
        return new Response(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

}
