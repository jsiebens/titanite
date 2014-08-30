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
package org.nosceon.titanite.auth;

import io.netty.handler.codec.http.HttpHeaders;
import org.nosceon.titanite.Filter;
import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;

import java.util.Base64;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.nosceon.titanite.Response.forbidden;
import static org.nosceon.titanite.Response.unauthorized;

/**
 * @author Johan Siebens
 */
public final class BasicAuthenticationFilter implements Filter {

    private static final String PREFIX = "Basic";

    private final BasicAuthenticator<?> authenticator;

    private final String challenge;

    private final Supplier<Response> accessDeniedHandler;

    public BasicAuthenticationFilter(BasicAuthenticator<?> authenticator) {
        this("Titanite", authenticator);
    }

    public BasicAuthenticationFilter(BasicAuthenticator<?> authenticator, Supplier<Response> accessDeniedHandler) {
        this("Titanite", authenticator, accessDeniedHandler);
    }

    public BasicAuthenticationFilter(String realm, BasicAuthenticator<?> authenticator) {
        this(realm, authenticator, () -> forbidden().text("Access Denied\n"));
    }

    public BasicAuthenticationFilter(String realm, BasicAuthenticator<?> authenticator, Supplier<Response> accessDeniedHandler) {
        this.authenticator = authenticator;
        this.challenge = String.format(PREFIX + " realm=\"%s\"", realm);
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    public CompletionStage<Response> apply(Request request, Function<Request, CompletionStage<Response>> handler) {
        String header = request.headers().getString(HttpHeaders.Names.AUTHORIZATION);

        Request req =
            request
                .withAttribute(Auth.UNAUTHORIZED, unauthorizedSupplier())
                .withAttribute(Auth.ACCESS_DENIED, accessDeniedSupplier());

        if (header != null) {
            final int space = header.indexOf(' ');
            if (space > 0) {
                String method = header.substring(0, space);
                if (PREFIX.equalsIgnoreCase(method)) {
                    String decoded = decode(header.substring(space + 1));
                    int i = decoded.indexOf(':');
                    if (i > 0) {
                        String username = decoded.substring(0, i);
                        String password = decoded.substring(i + 1);

                        return
                            authenticator
                                .authenticate(username, password)
                                .thenCompose(optAuth ->
                                    optAuth
                                        .map(auth -> handler.apply(req.withAttribute(Auth.ATTRIBUTE_ID, auth)))
                                        .orElseGet(() -> handler.apply(req)));

                    }
                }
            }
        }

        return handler.apply(req);
    }

    private Supplier<Response> unauthorizedSupplier() {
        return () -> unauthorized().header(HttpHeaders.Names.WWW_AUTHENTICATE, challenge);
    }

    private Supplier<Response> accessDeniedSupplier() {
        return accessDeniedHandler;
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value));
    }

}
