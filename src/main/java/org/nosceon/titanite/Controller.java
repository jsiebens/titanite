package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Johan Siebens
 */
public abstract class Controller extends Responses {

    private final List<Routing> routings = new LinkedList<>();

    protected final void get(String pattern, Supplier<Response> function) {
        routings.add(new Routing(HttpMethod.GET, pattern, r -> function.get()));
    }

    protected final void get(String pattern, Function<Request, Response> function) {
        routings.add(new Routing(HttpMethod.GET, pattern, function));
    }

    protected final void post(String pattern, Supplier<Response> function) {
        routings.add(new Routing(HttpMethod.POST, pattern, r -> function.get()));
    }

    protected final void post(String pattern, Function<Request, Response> function) {
        routings.add(new Routing(HttpMethod.POST, pattern, function));
    }

    protected final void put(String pattern, Supplier<Response> function) {
        routings.add(new Routing(HttpMethod.PUT, pattern, r -> function.get()));
    }

    protected final void put(String pattern, Function<Request, Response> function) {
        routings.add(new Routing(HttpMethod.PUT, pattern, function));
    }

    protected final void patch(String pattern, Supplier<Response> function) {
        routings.add(new Routing(HttpMethod.PATCH, pattern, r -> function.get()));
    }

    protected final void patch(String pattern, Function<Request, Response> function) {
        routings.add(new Routing(HttpMethod.PATCH, pattern, function));
    }

    protected final void delete(String pattern, Supplier<Response> function) {
        routings.add(new Routing(HttpMethod.DELETE, pattern, r -> function.get()));
    }

    protected final void delete(String pattern, Function<Request, Response> function) {
        routings.add(new Routing(HttpMethod.DELETE, pattern, function));
    }

    final List<Routing> routings() {
        return routings;
    }

}
