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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Method.POST;
import static org.nosceon.titanite.Titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class CustomFallbackTest extends AbstractE2ETest {

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) {
        return
            server
                .register(GET, "/a/b", (r) -> ok().text("A").toFuture())
                .register(POST, "/a/*path", (r) -> ok().text("B").toFuture())
                .register(GET, "/a/*path", (r) -> ok().text("C").toFuture())
                .start();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo("A")).when().get(uri("/a/b"));
        given().expect().statusCode(200).body(equalTo("B")).when().post(uri("/a/b"));
        given().expect().statusCode(405).when().put(uri("/a/b"));

        given().expect().statusCode(200).body(equalTo("C")).when().get(uri("/a/c"));
        given().expect().statusCode(404).when().get(uri("/b/c"));
    }

}
