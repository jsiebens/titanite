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

import org.nosceon.titanite.body.BodyParser;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Johan Siebens
 */
public abstract class Controller {

    private final List<Route> routings = new LinkedList<>();

    protected Controller() {
    }

    private Controller(List<Route> routes) {
        this.routings.addAll(routes);
    }

    protected final void head(String pattern, Function<Request, CompletionStage<Response>> handler) {
        routings.add(new Route(Method.HEAD, pattern, null, handler));
    }

    protected final void get(String pattern, Function<Request, CompletionStage<Response>> handler) {
        routings.add(new Route(Method.GET, pattern, null, handler));
    }

    protected final void post(String pattern, Function<Request, CompletionStage<Response>> handler) {
        routings.add(new Route(Method.POST, pattern, null, handler));
    }

    protected final void post(String pattern, Supplier<BodyParser> bodyParser, Function<Request, CompletionStage<Response>> handler) {
        routings.add(new Route(Method.POST, pattern, bodyParser, handler));
    }

    protected final void put(String pattern, Function<Request, CompletionStage<Response>> handler) {
        routings.add(new Route(Method.PUT, pattern, null, handler));
    }

    protected final void put(String pattern, Supplier<BodyParser> bodyParser, Function<Request, CompletionStage<Response>> handler) {
        routings.add(new Route(Method.PUT, pattern, bodyParser, handler));
    }

    protected final void patch(String pattern, Function<Request, CompletionStage<Response>> handler) {
        routings.add(new Route(Method.PATCH, pattern, null, handler));
    }

    protected final void patch(String pattern, Supplier<BodyParser> bodyParser, Function<Request, CompletionStage<Response>> handler) {
        routings.add(new Route(Method.PATCH, pattern, bodyParser, handler));
    }

    protected final void delete(String pattern, Function<Request, CompletionStage<Response>> handler) {
        routings.add(new Route(Method.DELETE, pattern, null, handler));
    }

    protected final void options(String pattern, Function<Request, CompletionStage<Response>> handler) {
        routings.add(new Route(Method.OPTIONS, pattern, null, handler));
    }

    final List<Route> routes() {
        return routings;
    }

}
