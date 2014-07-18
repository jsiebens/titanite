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

import static com.jayway.restassured.RestAssured.given;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Titanite.Responses.ok;
import static org.nosceon.titanite.scope.Session.*;

/**
 * @author Johan Siebens
 */
public class SessionTest extends AbstractE2ETest {

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) {
        return
            server
                .setFilter(enableSessions("my-favorite-secret"))
                .register(GET, "/a", (r) -> {
                    session(r).set("name", "titanite");
                    session(r).set("lorem", "ipsum");
                    return ok().toFuture();
                })
                .register(GET, "/b",
                    req -> {
                        int count = session(req).getInt("count", 0);
                        session(req).set("count", count + 1);
                        return ok().text(String.valueOf(count)).toFuture();
                    }
                )
                .start();
    }

    @Test
    public void testA() {
        given().expect().statusCode(200).cookie(DEFAULT_SESSION_COOKIE_NAME, "\"ddf5cc7e5f4a7d024bbf6cd3344fd6c306c5a20e|lorem=ipsum&name=titanite\"").when().get(uri("/a"));
    }

    @Test
    public void testB() {
        given()
            .cookie(DEFAULT_SESSION_COOKIE_NAME, "\"adfb9b2f6426f5b01d0bf50eb2e9c9fa9239d212|count=1\"")
            .expect().statusCode(200).cookie(DEFAULT_SESSION_COOKIE_NAME, "\"815077f6727696ac07ac5f5d3c6985c8485efcbf|count=2\"").when().get(uri("/b"));
    }

    @Test
    public void testC() {
        given()
            .cookie(DEFAULT_SESSION_COOKIE_NAME, "\"c1daaa1d944a6e97e79d7935bf72f0e265a26a07|lorem=ipsum\"")
            .expect().statusCode(200).cookie(DEFAULT_SESSION_COOKIE_NAME, "\"cb66ceedf8f49912d34468e38c99ee2b06b0337a|count=1&lorem=ipsum\"").when().get(uri("/b"));
    }

    @Test
    public void testUnsigned() {
        given()
            .cookie(DEFAULT_SESSION_COOKIE_NAME, "\"count=5\"")
            .expect().statusCode(200).cookie(DEFAULT_SESSION_COOKIE_NAME, "\"adfb9b2f6426f5b01d0bf50eb2e9c9fa9239d212|count=1\"").when().get(uri("/b"));
    }

    @Test
    public void testInvalidSignature() {
        given()
            .cookie(DEFAULT_SESSION_COOKIE_NAME, "\"invalid_signature|count=5\"")
            .expect().statusCode(200).cookie(DEFAULT_SESSION_COOKIE_NAME, "\"adfb9b2f6426f5b01d0bf50eb2e9c9fa9239d212|count=1\"").when().get(uri("/b"));
    }

}
