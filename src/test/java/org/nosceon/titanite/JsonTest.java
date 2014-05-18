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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nosceon.titanite.json.JsonMapper;
import org.nosceon.titanite.json.GsonMapper;
import org.nosceon.titanite.json.JacksonMapper;

import java.util.Arrays;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Method.POST;
import static org.nosceon.titanite.Titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
@RunWith(value = Parameterized.class)
public class JsonTest extends AbstractE2ETest {

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

    private JsonMapper m;

    public JsonTest(String name, JsonMapper mapper) {
        this.m = mapper;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> mappers() {
        return Arrays.asList(new Object[][]{
            {"jackson", new JacksonMapper()},
            {"gson", new GsonMapper()}
        });
    }

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(GET, "/json", (r) -> ok().body(m.out(new Hello("world"))).toFuture())
                .register(POST, "/json", (r) -> {
                    Hello hello = r.body().as(m.in(Hello.class));
                    return ok().body(m.out(new Hello(hello.getName().toUpperCase()))).toFuture();
                })
                .start();
    }

    @Test
    public void test() {
        given()
            .expect()
            .statusCode(200)
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
