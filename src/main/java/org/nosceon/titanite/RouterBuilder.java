package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static org.nosceon.titanite.Chain.newChain;
import static org.nosceon.titanite.service.ResourceService.PUBLIC_RESOURCES;
import static org.nosceon.titanite.service.ResourceService.WEBJAR_RESOURCES;

/**
 * @author Johan Siebens
 */
public abstract class RouterBuilder<R extends RouterBuilder> {

    private Function<Request, Response> fallback = newChain(PUBLIC_RESOURCES).fallbackTo(WEBJAR_RESOURCES);

    private final List<Routing<Request, Response>> routings = new LinkedList<>();

    private final List<Filter<Request, Response, Request, Response>> filters = new LinkedList<>();

    public final R get(String pattern, Function<Request, Response> function) {
        return register(HttpMethod.GET, pattern, function);
    }

    public final R post(String pattern, Function<Request, Response> function) {
        return register(HttpMethod.POST, pattern, function);
    }

    public final R put(String pattern, Function<Request, Response> function) {
        return register(HttpMethod.PUT, pattern, function);
    }

    public final R patch(String pattern, Function<Request, Response> function) {
        return register(HttpMethod.PATCH, pattern, function);
    }

    public final R delete(String pattern, Function<Request, Response> function) {
        return register(HttpMethod.DELETE, pattern, function);
    }

    public final R register(Routings<Request, Response> routings) {
        this.routings.addAll(routings.get());
        return self();
    }

    public final R register(Filter<Request, Response, Request, Response> filter) {
        this.filters.add(filter);
        return self();
    }

    public final R register(HttpMethod method, String pattern, Function<Request, Response> function) {
        this.routings.add(new Routing<>(method, pattern, function));
        return self();
    }

    public final R notFound(Function<Request, Response> fallback) {
        this.fallback = fallback;
        return self();
    }

    protected final Router router(String id) {
        return new Router(id, filters, routings, fallback);
    }

    protected abstract R self();

}
