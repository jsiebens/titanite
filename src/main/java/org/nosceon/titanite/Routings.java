package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public class Routings<I, O> extends Responses {

    private final List<Routing<I, O>> routings = new LinkedList<>();

    protected Routings() {
    }

    Routings(List<Routing<I, O>> routings) {
        this.routings.addAll(routings);
    }

    protected final void get(String pattern, Function<I, O> function) {
        routings.add(new Routing<>(HttpMethod.GET, pattern, function));
    }

    protected final void post(String pattern, Function<I, O> function) {
        routings.add(new Routing<>(HttpMethod.POST, pattern, function));
    }

    protected final void put(String pattern, Function<I, O> function) {
        routings.add(new Routing<>(HttpMethod.PUT, pattern, function));
    }

    protected final void patch(String pattern, Function<I, O> function) {
        routings.add(new Routing<>(HttpMethod.PATCH, pattern, function));
    }

    protected final void delete(String pattern, Function<I, O> function) {
        routings.add(new Routing<>(HttpMethod.DELETE, pattern, function));
    }

    final List<Routing<I, O>> get() {
        return routings;
    }

}
