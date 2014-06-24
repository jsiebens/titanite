/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static org.nosceon.titanite.Titanite.Responses.methodNotAllowed;

/**
 * @author Johan Siebens
 */
final class Router {

    public static final RoutingResult METHOD_NOT_ALLOWED = new RoutingResult(Collections.emptyMap(), (r) -> methodNotAllowed().toFuture());

    private final Map<ParameterizedPattern, Map<Method, Function<Request, CompletionStage<Response>>>> mapping = new LinkedHashMap<>();

    private final RoutingResult fallback;

    Router(
        String id,
        Optional<Filter> filter,
        List<Route> routings,
        Function<Request, CompletionStage<Response>> fallback) {

        this.fallback = new RoutingResult(Collections.emptyMap(), createFunction(filter, fallback));
        for (Route r : routings) {
            add(id, filter, r.method(), r.pattern(), r.function());
        }
    }

    RoutingResult find(HttpMethod method, String pattern) {
        Method map = map(method);
        if (map != null) {
            for (Map.Entry<ParameterizedPattern, Map<Method, Function<Request, CompletionStage<Response>>>> entry : mapping.entrySet()) {
                ParameterizedPattern.Matcher matcher = entry.getKey().matcher(pattern);
                if (matcher.matches()) {
                    Function<Request, CompletionStage<Response>> f = entry.getValue().get(map);
                    return f != null ? new RoutingResult(matcher.parameters(), f) : METHOD_NOT_ALLOWED;
                }
            }
            return fallback;
        }
        else {
            return METHOD_NOT_ALLOWED;
        }
    }

    private Router add(String id, Optional<Filter> filter, Method method, String pattern, Function<Request, CompletionStage<Response>> function) {
        ParameterizedPattern pp = new ParameterizedPattern(pattern);
        Map<Method, Function<Request, CompletionStage<Response>>> map = mapping.get(pp);
        if (map == null) {
            map = new HashMap<>();
            mapping.put(pp, map);
        }
        if (map.putIfAbsent(method, createFunction(filter, function)) == null) {
            Titanite.LOG.info(id + " route added: " + Utils.padEnd(method.toString(), 7, ' ') + pattern);
        }
        return this;
    }

    private Function<Request, CompletionStage<Response>> createFunction(Optional<Filter> filter, Function<Request, CompletionStage<Response>> function) {
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