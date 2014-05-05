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

import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Method.POST;

/**
 * @author Johan Siebens
 */
public class JsonResponseTest extends AbstractE2ETest {

    public static class Hello {

        private String name;

        public Hello() {
        }

        public Hello(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(GET, "/json", (r) -> ok().json(new Hello("world")).toFuture())
                .register(POST, "/json", (r) -> {
                    Hello hello = r.body.asJson(Hello.class);
                    return ok().json(new Hello(hello.getName().toUpperCase())).toFuture();
                })
                .start();
    }

    @Test
    public void test() {
        given()
            .expect()
            .statusCode(200)
            .header(HttpHeaders.Names.CONTENT_TYPE, equalTo("application/json"))
            .body(equalTo("{\"name\":\"world\"}"))
            .when().get(uri("/json"));

        given()
            .body("{\"name\":\"world\"}")
            .expect()
            .statusCode(200)
            .body(equalTo("{\"name\":\"WORLD\"}"))
            .when().post(uri("/json"));
    }

}
