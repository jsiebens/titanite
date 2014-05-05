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
import org.nosceon.titanite.view.View;
import org.nosceon.titanite.view.ViewTemplate;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;

/**
 * @author Johan Siebens
 */
public class ViewsTest extends AbstractE2ETest {

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

    @ViewTemplate("hello")
    public static class AnnotatedHelloView {

        private final String name;

        public AnnotatedHelloView(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public static class SubAnnotatedHelloView extends AnnotatedHelloView {

        public SubAnnotatedHelloView(String name) {
            super(name);
        }

    }

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(GET, "/hello1", (r) -> ok().view(new HelloView("hello", "world")).toFuture())
                .register(GET, "/hello2", (r) -> ok().view(new HelloView("hello.mustache", "world")).toFuture())
                .register(GET, "/hello3", (r) -> ok().view(new HelloView("/hello", "world")).toFuture())
                .register(GET, "/hello4", (r) -> ok().view(new AnnotatedHelloView("world")).toFuture())
                .register(GET, "/hello5", (r) -> ok().view(new SubAnnotatedHelloView("world")).toFuture())
                .register(GET, "/unavailable", (r) -> ok().view(new HelloView("unavailable", "world")).toFuture())
                .start();
    }

    @Test
    public void test() {
        given()
            .expect()
            .statusCode(200)
            .body(equalTo("Hello world")).when().get(uri("/hello1"));
        given()
            .expect()
            .statusCode(200)
            .body(equalTo("Hello world")).when().get(uri("/hello2"));
        given()
            .expect()
            .statusCode(200)
            .body(equalTo("Hello world")).when().get(uri("/hello3"));
        given()
            .expect()
            .statusCode(200)
            .body(equalTo("Hello world")).when().get(uri("/hello4"));
        given()
            .expect()
            .statusCode(200)
            .body(equalTo("Hello world")).when().get(uri("/hello5"));
        given()
            .expect()
            .statusCode(500)
            .when().get(uri("/unavailable"));
    }

}
