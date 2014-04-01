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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;

/**
 * @author Johan Siebens
 */
public class ViewsTest extends AbstractE2ETest {

    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    public static class HelloView extends View {

        private final String name;

        public HelloView(String template, String name) {
            super(template);
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(GET, "/hello1", (r) -> ok().view(new HelloView("hello", "world")).toFuture())
                .register(GET, "/hello2", (r) -> ok().view(new HelloView("hello.mustache", "world")).toFuture())
                .register(GET, "/hello3", (r) -> ok().view(new HelloView("/hello", "world")).toFuture())
                .register(GET, "/unavailable", (r) -> ok().view(new HelloView("unavailable", "world")).toFuture())
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given()
            .expect()
            .statusCode(200)
            .body(equalTo("Hello world")).when().get(uri(port, "/hello1"));
        given()
            .expect()
            .statusCode(200)
            .body(equalTo("Hello world")).when().get(uri(port, "/hello2"));
        given()
            .expect()
            .statusCode(200)
            .body(equalTo("Hello world")).when().get(uri(port, "/hello3"));
        given()
            .expect()
            .statusCode(500)
            .when().get(uri(port, "/unavailable"));
    }

}
