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
package org.nosceon.titanite.scope;

import org.nosceon.titanite.*;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.Utils.checkNotEmpty;
import static org.nosceon.titanite.Utils.serialize;

/**
 * @author Johan Siebens
 */
public final class FlashFilter implements BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> {

    public static final String DEFAULT_FLASH_COOKIE_NAME = "_flash";

    private final String cookieName;

    public FlashFilter() {
        this(DEFAULT_FLASH_COOKIE_NAME);
    }

    public FlashFilter(String cookieName) {
        this.cookieName = checkNotEmpty(cookieName, "cookieName is required");
    }

    @Override
    public CompletionStage<Response> apply(Request request, Function<Request, CompletionStage<Response>> function) {
        Flash scope =
            ofNullable(request.cookies().getString(cookieName))
                .map(Utils::deserialize)
                .map(Flash::new)
                .orElseGet(Flash::new);

        return function.apply(request.withAttribute(Flash.ATTRIBUTE_ID, scope)).thenApply(
            resp -> resp.cookie(new Cookie(cookieName, serialize(scope.values())).httpOnly(true).path("/"))
        );
    }

}
