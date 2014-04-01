/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
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
        routings.add(new Routing<>(Method.GET, pattern, function));
    }

    protected final void post(String pattern, Function<I, O> function) {
        routings.add(new Routing<>(Method.POST, pattern, function));
    }

    protected final void put(String pattern, Function<I, O> function) {
        routings.add(new Routing<>(Method.PUT, pattern, function));
    }

    protected final void patch(String pattern, Function<I, O> function) {
        routings.add(new Routing<>(Method.PATCH, pattern, function));
    }

    protected final void delete(String pattern, Function<I, O> function) {
        routings.add(new Routing<>(Method.DELETE, pattern, function));
    }

    final List<Routing<I, O>> get() {
        return routings;
    }

}
