package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;

import java.util.function.Function;

/**
 * @author Johan Siebens
 */
final class Routing<I, O> {

    private final HttpMethod method;

    private final String pattern;

    private final Function<I, O> function;

    Routing(HttpMethod method, String pattern, Function<I, O> function) {
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

    public Function<I, O> function() {
        return function;
    }

}
