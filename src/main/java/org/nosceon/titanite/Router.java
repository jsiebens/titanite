package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

import static org.nosceon.titanite.Response.methodNotAllowed;
import static org.nosceon.titanite.Response.notFound;

/**
 * @author Johan Siebens
 */
final class Router {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private static class MethodSelector implements Selector {

        private final Map<HttpMethod, Selector> selectors = new HashMap<>();

        @Override
        public Function<Request, Response> get(HttpMethod method, String path) {
            return selectors.containsKey(method) ? selectors.get(method).get(method, path) : (r) -> methodNotAllowed();
        }

        public boolean add(HttpMethod method, Selector selector) {
            return selectors.putIfAbsent(method, selector) == null;
        }

    }

    public Router(List<Routing> routings) {
        for (Routing r : routings) {
            add(r.method(), r.pattern(), r.function());
        }
    }

    private final Map<ParameterizedPattern, MethodSelector> mapping = new LinkedHashMap<>();

    public RoutingResult find(String pattern) {
        for (Map.Entry<ParameterizedPattern, MethodSelector> entry : mapping.entrySet()) {
            ParameterizedPattern.Matcher matcher = entry.getKey().matcher(pattern);
            if (matcher.matches()) {
                return new RoutingResult(matcher.parameters(), entry.getValue());
            }
        }
        return new RoutingResult(Collections.emptyMap(), (method, path) -> (r) -> notFound());
    }

    private Router add(HttpMethod method, String pattern, Function<Request, Response> function) {
        return add(method, pattern, (m, p) -> function);
    }

    private Router add(HttpMethod method, String pattern, Selector selector) {
        ParameterizedPattern pp = new ParameterizedPattern(pattern);
        MethodSelector byMethod = mapping.get(pp);
        if (byMethod == null) {
            byMethod = new MethodSelector();
            mapping.put(pp, byMethod);
        }
        if (byMethod.add(method, selector)) {
            log.info("Http Server registered handler for " + method + " " + pattern);
        }
        return this;
    }

}
