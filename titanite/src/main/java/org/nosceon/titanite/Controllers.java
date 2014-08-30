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
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public final class Controllers {

    public static Controller HEAD(String pattern, Function<Request, CompletionStage<Response>> handler) {
        return new Controller() {{
            head(pattern, handler);
        }};
    }

    public static Controller GET(String pattern, Function<Request, CompletionStage<Response>> handler) {
        return new Controller() {{
            get(pattern, handler);
        }};
    }

    public static Controller POST(String pattern, Function<Request, CompletionStage<Response>> handler) {
        return new Controller() {{
            post(pattern, handler);
        }};
    }

    public static Controller PUT(String pattern, Function<Request, CompletionStage<Response>> handler) {
        return new Controller() {{
            put(pattern, handler);
        }};
    }

    public static Controller PATCH(String pattern, Function<Request, CompletionStage<Response>> handler) {
        return new Controller() {{
            patch(pattern, handler);
        }};
    }

    public static Controller DELETE(String pattern, Function<Request, CompletionStage<Response>> handler) {
        return new Controller() {{
            delete(pattern, handler);
        }};
    }

    public static Controller OPTIONS(String pattern, Function<Request, CompletionStage<Response>> handler) {
        return new Controller() {{
            options(pattern, handler);
        }};
    }

}
