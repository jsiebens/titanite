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

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.nosceon.titanite.Titanite.Responses.*;

/**
 * @author Johan Siebens
 */
final class Router {

    private static final RoutingResult METHOD_NOT_ALLOWED = new RoutingResult(emptyMap(), (r) -> methodNotAllowed().toFuture());

    private static final RoutingResult NOT_FOUND = new RoutingResult(emptyMap(), (r) -> notFound().toFuture());

    private final List<Route> routes = new LinkedList<>();

    Router(List<Route> routings) {
        this.routes.addAll(routings);
    }

    RoutingResult find(HttpMethod httpMethod, String path) {
        Method method = map(httpMethod);

        List<Route> candidates =
            this.routes
                .stream()
                .filter(r -> r.pattern().matches(path))
                .collect(toList());

        if (candidates.isEmpty()) {
            return NOT_FOUND;
        }
        else {
            return
                candidates
                    .stream()
                    .filter(route -> route.hasMethod(method))
                    .findFirst()
                    .map(route -> {
                        ParameterizedPattern.Matcher matcher = route.pattern().matcher(path);
                        return
                            Method.OPTIONS.equals(method) ?
                                new RoutingResult(matcher.parameters(), Utils.compose(allowedMethodsFilter(candidates), route.function())) :
                                new RoutingResult(matcher.parameters(), route.function());
                    })
                    .orElseGet(() -> {
                            if (Method.OPTIONS.equals(method)) {
                                return new RoutingResult(emptyMap(), Utils.compose(allowedMethodsFilter(candidates), req -> ok().toFuture()));
                            }
                            else if (Method.HEAD.equals(method)) {
                                return find(HttpMethod.GET, path);
                            }
                            else {
                                return METHOD_NOT_ALLOWED;
                            }
                        }
                    );
        }

    }

    private BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> allowedMethodsFilter(List<Route> candidates) {
        return (req, h) -> h.apply(req).thenApply(resp -> resp.header(HttpHeaders.Names.ALLOW, allowedMethods(candidates)));
    }

    private String allowedMethods(List<Route> candidates) {
        return candidates.stream().map(Route::method).distinct().map(Method::name).sorted().reduce("", (s, s2) -> s.length() == 0 ? s2 : s + ", " + s2);
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
