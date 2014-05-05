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

import io.netty.handler.codec.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletionStage;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.*;

/**
 * @author Johan Siebens
 */
public class MethodsTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    public static class MyController extends Controller {

        public MyController() {
            get("/controller", this::handle);
            post("/controller", this::handle);
            put("/controller", this::handle);
            delete("/controller", this::handle);
            patch("/controller", this::handle);
        }

        private CompletionStage<Response> handle(Request request) {
            return ok().body(request.method.name()).toFuture();
        }

    }

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer(port)
                .register(GET, "/resource", (r) -> ok().body(r.method.name()).toFuture())
                .register(POST, "/resource", (r) -> ok().body(r.method.name()).toFuture())
                .register(PUT, "/resource", (r) -> ok().body(r.method.name()).toFuture())
                .register(DELETE, "/resource", (r) -> ok().body(r.method.name()).toFuture())
                .register(PATCH, "/resource", (r) -> ok().body(r.method.name()).toFuture())
                .register(new MyController())
                .start();
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo(HttpMethod.GET.name())).when().get(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.POST.name())).when().post(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PUT.name())).when().put(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.DELETE.name())).when().delete(uri(port, "/resource"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PATCH.name())).when().patch(uri(port, "/resource"));

        given().expect().statusCode(200).body(equalTo(HttpMethod.GET.name())).when().get(uri(port, "/controller"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.POST.name())).when().post(uri(port, "/controller"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PUT.name())).when().put(uri(port, "/controller"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.DELETE.name())).when().delete(uri(port, "/controller"));
        given().expect().statusCode(200).body(equalTo(HttpMethod.PATCH.name())).when().patch(uri(port, "/controller"));
    }

}
