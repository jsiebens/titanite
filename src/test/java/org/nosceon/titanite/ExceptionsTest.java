/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nosceon.titanite;

import org.junit.Test;

import java.util.concurrent.CompletionException;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Titanite.errors;

/**
 * @author Johan Siebens
 */
public class ExceptionsTest extends AbstractE2ETest {

    public static class InternalException extends RuntimeException {

    }

    public static class InternalSub1Exception extends InternalException {

    }

    public static class InternalSub2Exception extends InternalException {

    }

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) {
        return
            server
                .setFilter(
                    errors()
                        .match(InternalException.class, (r, e) -> ok().text("Internal"))
                        .match(InternalSub1Exception.class, () -> ok().text("Internal Sub1"))
                )
                .register(GET, "/a", (r) -> {
                    throw new RuntimeException();
                })
                .register(GET, "/b", (r) -> {
                    throw new CompletionException(new RuntimeException());
                })
                .register(GET, "/c", (r) -> {
                    throw new HttpServerException(Responses.status(503));
                })
                .register(GET, "/d", (r) -> {
                    throw new CompletionException(new InternalSub1Exception());
                })
                .register(GET, "/e", (r) -> {
                    throw new InternalSub2Exception();
                })
                .start();
    }

    @Test
    public void test() {
        given().expect().statusCode(500).when().get(uri("/a"));
        given().expect().statusCode(500).when().get(uri("/b"));
        given().expect().statusCode(503).when().get(uri("/c"));

        given().expect().statusCode(200).body(equalTo("Internal Sub1")).when().get(uri("/d"));
        given().expect().statusCode(200).body(equalTo("Internal")).when().get(uri("/e"));
    }

}
