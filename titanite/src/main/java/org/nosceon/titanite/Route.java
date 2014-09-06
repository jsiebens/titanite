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

import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Johan Siebens
 */
final class Route {

    private final Method method;

    private final ParameterizedPattern pattern;

    private final Supplier<BodyParser> bodyParser;

    private final Function<Request, CompletionStage<Response>> handler;

    Route(Method method, String pattern, Supplier<BodyParser> bodyParser, Function<Request, CompletionStage<Response>> handler) {
        this(method, new ParameterizedPattern(pattern), bodyParser, handler);
    }

    Route(Method method, ParameterizedPattern pattern, Supplier<BodyParser> bodyParser, Function<Request, CompletionStage<Response>> handler) {
        this.method = method;
        this.pattern = pattern;
        this.bodyParser = bodyParser;
        this.handler = handler;
    }

    public Method method() {
        return method;
    }

    public ParameterizedPattern pattern() {
        return pattern;
    }

    public Supplier<BodyParser> bodyParser() {
        return bodyParser;
    }

    public Function<Request, CompletionStage<Response>> handler() {
        return handler;
    }

    public boolean hasMethod(Method method) {
        return this.method.equals(method);
    }

}
