package org.nosceon.titanite;

import com.google.common.base.Strings;
import io.netty.handler.codec.http.HttpMethod;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.nosceon.titanite.Responses.methodNotAllowed;

/**
 * @author Johan Siebens
 */
public final class Router {

    public static final RoutingResult METHOD_NOT_ALLOWED = new RoutingResult(Collections.emptyMap(), (r) -> methodNotAllowed().toFuture());

    private final Map<ParameterizedPattern, Map<Method, Function<Request, CompletableFuture<Response>>>> mapping = new LinkedHashMap<>();

    private final RoutingResult fallback;

    private final String id;

    public Router(
        String id,
        Optional<Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>>> filter,
        List<Routing<Request, CompletableFuture<Response>>> routings,
        Function<Request, CompletableFuture<Response>> fallback) {

        this.id = id;
        this.fallback = new RoutingResult(Collections.emptyMap(), createFunction(filter, fallback));
        for (Routing<Request, CompletableFuture<Response>> r : routings) {
            add(filter, r.method(), r.pattern(), r.function());
        }
    }

    RoutingResult find(HttpMethod method, String pattern) {
        Method map = map(method);
        if (map != null) {
            for (Map.Entry<ParameterizedPattern, Map<Method, Function<Request, CompletableFuture<Response>>>> entry : mapping.entrySet()) {
                ParameterizedPattern.Matcher matcher = entry.getKey().matcher(pattern);
                if (matcher.matches()) {
                    Function<Request, CompletableFuture<Response>> f = entry.getValue().get(map);
                    return f != null ? new RoutingResult(matcher.parameters(), f) : METHOD_NOT_ALLOWED;
                }
            }
            return fallback;
        }
        else {
            return METHOD_NOT_ALLOWED;
        }
    }

    private Router add(Optional<Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>>> filter, Method method, String pattern, Function<Request, CompletableFuture<Response>> function) {
        ParameterizedPattern pp = new ParameterizedPattern(pattern);
        Map<Method, Function<Request, CompletableFuture<Response>>> map = mapping.get(pp);
        if (map == null) {
            map = new HashMap<>();
            mapping.put(pp, map);
        }
        if (map.putIfAbsent(method, createFunction(filter, function)) == null) {
            Titanite.LOG.info("Router [" + id + "] registered handler for " + Strings.padEnd(method.toString(), 6, ' ') + " " + pattern);
        }
        return this;
    }

    private Function<Request, CompletableFuture<Response>> createFunction(Optional<Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>>> filter, Function<Request, CompletableFuture<Response>> function) {
        return filter.isPresent() ? filter.get().andThen(function) : function;
    }

    private Method map(HttpMethod method) {
        try {
            return Method.valueOf(method.name());
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

}
