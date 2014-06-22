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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public abstract class Controller {

    static Controller newController(List<Route> routes) {
        return new InternalController(routes);
    }

    private final List<Route> routings = new LinkedList<>();

    protected Controller() {
    }

    private Controller(List<Route> routes) {
        this.routings.addAll(routes);
    }

    protected final void get(String pattern, Function<Request, CompletionStage<Response>> function) {
        routings.add(new Route(Method.GET, pattern, function));
    }

    protected final void post(String pattern, Function<Request, CompletionStage<Response>> function) {
        routings.add(new Route(Method.POST, pattern, function));
    }

    protected final void put(String pattern, Function<Request, CompletionStage<Response>> function) {
        routings.add(new Route(Method.PUT, pattern, function));
    }

    protected final void patch(String pattern, Function<Request, CompletionStage<Response>> function) {
        routings.add(new Route(Method.PATCH, pattern, function));
    }

    protected final void delete(String pattern, Function<Request, CompletionStage<Response>> function) {
        routings.add(new Route(Method.DELETE, pattern, function));
    }

    final List<Route> get() {
        return routings;
    }

    private static class InternalController extends Controller {

        private InternalController(List<Route> routes) {
            super(routes);
        }

    }

}
