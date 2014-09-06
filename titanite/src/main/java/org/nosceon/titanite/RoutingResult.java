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

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Johan Siebens
 */
final class RoutingResult {

    private final Map<String, String> pathParams;

    private final Function<Request, CompletionStage<Response>> handler;

    private final Supplier<BodyParser> bodyParser;

    public RoutingResult(Map<String, String> pathParams, Supplier<BodyParser> bodyParser, Function<Request, CompletionStage<Response>> handler) {
        this.pathParams = pathParams;
        this.bodyParser = bodyParser;
        this.handler = handler;
    }

    public Map<String, String> pathParams() {
        return pathParams;
    }

    public Function<Request, CompletionStage<Response>> handler() {
        return handler;
    }

    public Supplier<BodyParser> bodyParser() {
        return bodyParser;
    }

}
