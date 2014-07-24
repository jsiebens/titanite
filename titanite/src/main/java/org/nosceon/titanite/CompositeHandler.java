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

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.nosceon.titanite.Utils.checkNotNull;

/**
 * @author Johan Siebens
 */
public final class CompositeHandler implements Function<Request, CompletionStage<Response>> {

    @SafeVarargs
    public static Function<Request, CompletionStage<Response>> h(
        Function<Request, CompletionStage<Response>> first,
        Function<Request, CompletionStage<Response>> second,
        Function<Request, CompletionStage<Response>>... next) {
        return handlers(first, second, next);
    }

    @SafeVarargs
    public static Function<Request, CompletionStage<Response>> handlers(
        Function<Request, CompletionStage<Response>> first,
        Function<Request, CompletionStage<Response>> second,
        Function<Request, CompletionStage<Response>>... next) {
        return new CompositeHandler(first, second, next);
    }

    private final Function<Request, CompletionStage<Response>> composition;

    @SafeVarargs
    public CompositeHandler(
        Function<Request, CompletionStage<Response>> first,
        Function<Request, CompletionStage<Response>> second,
        Function<Request, CompletionStage<Response>>... next) {

        checkNotNull(first, "first handler is required");
        checkNotNull(second, "second handler is required");

        Function<Request, CompletionStage<Response>> c = compose(first, second);

        if (next != null) {
            for (Function<Request, CompletionStage<Response>> n : next) {
                c = compose(c, n);
            }
        }

        this.composition = c;
    }

    public CompositeHandler(
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter,
        Function<Request, CompletionStage<Response>> handler) {

        checkNotNull(filter, "filter is required");
        checkNotNull(handler, "handler is required");

        this.composition = request -> filter.apply(request, handler);
    }

    @Override
    public CompletionStage<Response> apply(Request request) {
        return composition.apply(request);
    }

    private static Function<Request, CompletionStage<Response>> compose(Function<Request, CompletionStage<Response>> first, Function<Request, CompletionStage<Response>> second) {
        return (request) ->
            first.apply(request)
                .thenCompose(resp -> {
                    if (resp == null || resp.status() == 404) {
                        return second.apply(request);
                    }
                    return completedFuture(resp);
                });

    }

}
