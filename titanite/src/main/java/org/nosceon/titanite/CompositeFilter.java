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

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.nosceon.titanite.Utils.checkNotNull;

/**
 * @author Johan Siebens
 */
public final class CompositeFilter implements BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> {

    @SafeVarargs
    public static BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> f(
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> first,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> second,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>>... next) {
        return filters(first, second, next);
    }

    @SafeVarargs
    public static BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filters(
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> first,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> second,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>>... next) {
        return new CompositeFilter(first, second, next);
    }

    private final BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> composition;

    @SafeVarargs
    public CompositeFilter(
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> first,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> second,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>>... next) {

        checkNotNull(first, "first filter is required");
        checkNotNull(second, "second filter is required");

        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> c = compose(first, second);

        if (next != null) {
            for (BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> n : next) {
                c = compose(c, n);
            }
        }

        this.composition = c;
    }

    @Override
    public CompletionStage<Response> apply(Request request, Function<Request, CompletionStage<Response>> handler) {
        return this.composition.apply(request, handler);
    }

    private static BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> compose(
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> first,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> second) {
        return (request, next) -> first.apply(request, (r) -> second.apply(r, next));
    }

}
