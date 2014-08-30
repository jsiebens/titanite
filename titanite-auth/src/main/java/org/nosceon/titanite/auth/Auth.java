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

import org.nosceon.titanite.Filter;
import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Johan Siebens
 */
public class Auth {

    public static final String ATTRIBUTE_ID = Auth.class.getName();

    static final String UNAUTHORIZED = ATTRIBUTE_ID + "_unauthorized";

    static final String ACCESS_DENIED = ATTRIBUTE_ID + "_forbidden";

    public static <T> T authentication(Request request) {
        return request.attributes().get(ATTRIBUTE_ID);
    }

    public static Filter isAuthenticated() {
        return (request, handler) -> {
            if (request.attributes().get(ATTRIBUTE_ID) != null) {
                return handler.apply(request);
            }
            else {
                return unauthorizedResponseSupplier(request).get().toFuture();
            }
        };
    }

    public static Filter hasRole(String role) {
        return (request, handler) -> {
            Object authentication = authentication(request);
            if (authentication == null) {
                return unauthorizedResponseSupplier(request).get().toFuture();
            }
            else if (authentication instanceof HasRoles && ((HasRoles) authentication).getRoles().contains(role)) {
                return handler.apply(request);
            }
            else {
                return accessDeniedResponseSupplier(request).get().toFuture();
            }
        };
    }

    public static Filter hasAnyRole(String role, String... otherRoles) {
        if (otherRoles == null || otherRoles.length == 0) {
            return hasRole(role);
        }
        else {
            Set<String> expectedRoles = expectedRoles(role, otherRoles);
            return (request, handler) -> {
                Object authentication = authentication(request);
                if (authentication == null) {
                    return unauthorizedResponseSupplier(request).get().toFuture();
                }
                else if (authentication instanceof HasRoles) {
                    List<String> actualRoles = ((HasRoles) authentication).getRoles();
                    if (actualRoles.stream().filter(expectedRoles::contains).findAny().isPresent()) {
                        return handler.apply(request);
                    }
                }
                return accessDeniedResponseSupplier(request).get().toFuture();
            };
        }
    }

    private static Set<String> expectedRoles(String role, String[] otherRoles) {
        Set<String> roles = new HashSet<>();
        roles.add(role);
        if (otherRoles != null) {
            roles.addAll(Arrays.asList(otherRoles));
        }
        return roles;
    }

    @SuppressWarnings("unchecked")
    private static Supplier<Response> unauthorizedResponseSupplier(Request request) {
        return (Supplier<Response>) request.attributes().get(UNAUTHORIZED);
    }

    @SuppressWarnings("unchecked")
    private static Supplier<Response> accessDeniedResponseSupplier(Request request) {
        return (Supplier<Response>) request.attributes().get(ACCESS_DENIED);
    }

}
