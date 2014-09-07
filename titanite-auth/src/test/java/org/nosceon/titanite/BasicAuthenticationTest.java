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

import org.junit.Test;
import org.nosceon.titanite.auth.basic.BasicAuthentication;
import org.nosceon.titanite.auth.basic.BasicAuthenticator;
import org.nosceon.titanite.auth.HasRoles;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.jayway.restassured.RestAssured.given;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Response.ok;
import static org.nosceon.titanite.auth.Auth.*;

/**
 * @author Johan Siebens
 */
public class BasicAuthenticationTest extends AbstractE2ETest {

    public static class User implements HasRoles {

        @Override
        public List<String> getRoles() {
            return Collections.singletonList("user");
        }

    }

    public static class Admin implements HasRoles {

        @Override
        public List<String> getRoles() {
            return Collections.singletonList("admin");
        }

    }

    public static class UserAuthenticator implements BasicAuthenticator<Object> {

        @Override
        public CompletionStage<Optional<Object>> authenticate(String username, String password) {
            switch (username) {
                case "admin":
                    return CompletableFuture.completedFuture(Optional.of(new Admin()));
                case "user":
                    return CompletableFuture.completedFuture(Optional.of(new User()));
                default:
                    return CompletableFuture.completedFuture(Optional.empty());
            }
        }

    }

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {

        BasicAuthentication basicAuth = new BasicAuthentication(new UserAuthenticator());

        return
            server
                .setFilter(basicAuth)
                .register(GET, "/a", req -> ok().text("ok").toFuture())
                .register(GET, "/b", isAuthenticated(), req -> ok().text("ok").toFuture())
                .register(GET, "/c", hasRole("user"), req -> ok().text("ok").toFuture())
                .register(GET, "/d", hasRole("admin"), req -> ok().text("ok").toFuture())
                .register(GET, "/e", hasAnyRole("user", "admin"), req -> ok().text("ok").toFuture())
                .start();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).when().get(uri("/a"));

        given().expect().statusCode(401).when().get(uri("/b"));
        given().auth().basic("user", "user").expect().statusCode(200).when().get(uri("/b"));
        given().auth().basic("admin", "admin").expect().statusCode(200).when().get(uri("/b"));
        given().auth().basic("johndoe", "johndoe").expect().statusCode(401).when().get(uri("/b"));

        given().expect().statusCode(401).when().get(uri("/c"));
        given().auth().basic("user", "user").expect().statusCode(200).when().get(uri("/c"));
        given().auth().basic("admin", "admin").expect().statusCode(403).when().get(uri("/c"));
        given().auth().basic("johndoe", "johndoe").expect().statusCode(401).when().get(uri("/c"));

        given().expect().statusCode(401).when().get(uri("/d"));
        given().auth().basic("user", "user").expect().statusCode(403).when().get(uri("/d"));
        given().auth().basic("admin", "admin").expect().statusCode(200).when().get(uri("/d"));
        given().auth().basic("johndoe", "johndoe").expect().statusCode(401).when().get(uri("/d"));

        given().expect().statusCode(401).when().get(uri("/e"));
        given().auth().basic("user", "user").expect().statusCode(200).when().get(uri("/e"));
        given().auth().basic("admin", "admin").expect().statusCode(200).when().get(uri("/e"));
        given().auth().basic("johndoe", "johndoe").expect().statusCode(401).when().get(uri("/e"));
    }

}
