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

import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Response.ok;

/**
 * @author Johan Siebens
 */
public class SimpleResponseTest extends AbstractE2ETest {

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(GET, "/text", (r) -> ok().text("Hello World").toFuture())
                .register(GET, "/html", (r) -> ok().html("<h1>Hello World</h1>").toFuture())
                .start();
    }

    @Test
    public void test() {
        given()
            .expect()
            .header(HttpHeaders.Names.CONTENT_TYPE, "text/plain")
            .statusCode(200).body(equalTo("Hello World")).when().get(uri("/text"));

        given()
            .expect()
            .header(HttpHeaders.Names.CONTENT_TYPE, "text/html")
            .statusCode(200).body(equalTo("<h1>Hello World</h1>")).when().get(uri("/html"));
    }

}
