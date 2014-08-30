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

/**
 * @author Johan Siebens
 */
public final class Filters {

    public static PatternMatchingFilter f(BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter) {
        return new PatternMatchingFilter(filter);
    }

    public static PatternMatchingFilter filter(BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter) {
        return new PatternMatchingFilter(filter);
    }

    @SafeVarargs
    public static PatternMatchingFilter f(
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> first,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> second,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>>... next) {
        return filters(first, second, next);
    }

    @SafeVarargs
    public static PatternMatchingFilter filters(
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> first,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> second,
        BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>>... next) {
        return new PatternMatchingFilter(new CompositeFilter(first, second, next));
    }

    public static ExceptionsFilter onException() {
        return new ExceptionsFilter();
    }

    private Filters() {
    }

}
