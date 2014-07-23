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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public final class Titanite {

    static final Logger LOG = LoggerFactory.getLogger(Titanite.class);

    @SafeVarargs
    public static Function<Request, CompletionStage<Response>> $(Function<Request, CompletionStage<Response>> handler, Function<Request, CompletionStage<Response>>... handlers) {
        return Utils.compose(handler, handlers);
    }

    @SafeVarargs
    public static BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> $(BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter, BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>>... filters) {
        return Utils.compose(filter, filters);
    }

}
