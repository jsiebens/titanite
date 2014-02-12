package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;

import java.util.function.Function;

/**
 * @author Johan Siebens
 */
final class Routing {

    private final HttpMethod method;

    private final String pattern;

    private final Function<Request, Response> function;

    Routing(HttpMethod method, String pattern, Function<Request, Response> function) {
        this.method = method;
        this.pattern = pattern;
        this.function = function;
    }

    public HttpMethod method() {
        return method;
    }

    public String pattern() {
        return pattern;
    }

    public Function<Request, Response> function() {
        return function;
    }

}
