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
package org.nosceon.titanite.auth.basic;

import io.netty.handler.codec.http.HttpHeaders;
import org.nosceon.titanite.Filter;
import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;
import org.nosceon.titanite.auth.Auth;

import java.util.Base64;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static org.nosceon.titanite.Response.forbidden;
import static org.nosceon.titanite.Response.unauthorized;

/**
 * @author Johan Siebens
 */
public final class BasicAuthentication implements Filter {

    private static final String PREFIX = "Basic";

    private final BasicAuthenticator<?> authenticator;

    private final String challenge;

    private final Function<Request, CompletionStage<Response>> accessDeniedHandler;

    public BasicAuthentication(BasicAuthenticator<?> authenticator) {
        this("Titanite", authenticator);
    }

    public BasicAuthentication(BasicAuthenticator<?> authenticator, Function<Request, CompletionStage<Response>> accessDeniedHandler) {
        this("Titanite", authenticator, accessDeniedHandler);
    }

    public BasicAuthentication(String realm, BasicAuthenticator<?> authenticator) {
        this(realm, authenticator, request -> forbidden().text("Access Denied\n").toFuture());
    }

    public BasicAuthentication(String realm, BasicAuthenticator<?> authenticator, Function<Request, CompletionStage<Response>> accessDeniedHandler) {
        this.authenticator = authenticator;
        this.challenge = String.format(PREFIX + " realm=\"%s\"", realm);
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    public CompletionStage<Response> apply(Request request, Function<Request, CompletionStage<Response>> handler) {
        String header = request.headers().getString(HttpHeaders.Names.AUTHORIZATION);

        Request req =
            request
                .withAttribute(Auth.UNAUTHORIZED_ATTRIBUTE_ID, unauthorizedHandler())
                .withAttribute(Auth.ACCESS_DENIED_ATTRIBUTE_ID, accessDeniedHandler());

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

    private Function<Request, CompletionStage<Response>> unauthorizedHandler() {
        return request -> unauthorized().header(HttpHeaders.Names.WWW_AUTHENTICATE, challenge).toFuture();
    }

    private Function<Request, CompletionStage<Response>> accessDeniedHandler() {
        return accessDeniedHandler;
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value));
    }

}
