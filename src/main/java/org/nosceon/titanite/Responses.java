package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpResponseStatus;

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

    public static Response internalServerError() {
        return new Response(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    public static Response notImplemented() {
        return new Response(HttpResponseStatus.NOT_IMPLEMENTED);
    }

}
