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

import org.nosceon.titanite.Cookie;
import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;
import org.nosceon.titanite.Utils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.Utils.*;

/**
 * @author Johan Siebens
 */
public final class SessionFilter implements BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> {

    public static final String DEFAULT_SESSION_COOKIE_NAME = "_session";

    private final String cookieName;

    private final String secret;

    public SessionFilter(String secret) {
        this(DEFAULT_SESSION_COOKIE_NAME, secret);
    }

    public SessionFilter(String cookieName, String secret) {
        this.cookieName = checkNotEmpty(cookieName, "cookieName is required");
        this.secret = checkNotEmpty(secret, "secret is required");
    }

    @Override
    public CompletionStage<Response> apply(Request request, Function<Request, CompletionStage<Response>> function) {
        Session scope =
            ofNullable(request.cookies().getString(cookieName))
                .map(this::decode)
                .map(Session::new)
                .orElseGet(Session::new);

        return function.apply(request.withAttribute(Session.ATTRIBUTE_ID, scope)).thenApply(
            resp -> resp.cookie(encode(scope.values()))
        );
    }

    Cookie encode(Map<String, String> values) {
        String serialized = serialize(values);
        String signed = sign(secret, serialized) + '|' + serialized;
        return new Cookie(cookieName, signed).httpOnly(true).path("/");
    }

    private Map<String, String> decode(String value) {
        int i = value.indexOf('|');

        if (i == -1) {
            return Collections.emptyMap();
        }

        String signature = value.substring(0, i);
        String values = value.substring(i + 1);

        return verify(secret, signature, values) ? deserialize(values) : Collections.emptyMap();
    }

}
