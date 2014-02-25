package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpResponseStatus;

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

    public static Response badRequest() {
        return new Response(HttpResponseStatus.BAD_REQUEST);
    }

    public static Response notFound() {
        return new Response(HttpResponseStatus.NOT_FOUND);
    }

    public static Response notModified() {
        return new Response(HttpResponseStatus.NOT_MODIFIED);
    }

    public static Response methodNotAllowed() {
        return new Response(HttpResponseStatus.METHOD_NOT_ALLOWED);
    }

    public static Response unsupportedMediaType() {
        return new Response(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    public static Response internalServerError() {
        return new Response(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

}
