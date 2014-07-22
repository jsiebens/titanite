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

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Method.POST;
import static org.nosceon.titanite.Titanite.$;
import static org.nosceon.titanite.Titanite.Responses.notFound;
import static org.nosceon.titanite.Titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class CustomFallbackTest extends AbstractE2ETest {

    public static class Handler implements Function<Request, CompletionStage<Response>> {

        private String value;

        public Handler(String value) {
            this.value = value;
        }

        @Override
        public CompletionStage<Response> apply(Request request) {
            if (value.equals(request.queryParams().getString("v"))) {
                return ok().body(value).toFuture();
            }
            else {
                return notFound().toFuture();
            }
        }

    }

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) {
        return
            server
                .register(GET, "/a", $(new Handler("A"), new Handler("B"), new Handler("C")))
                .start();
    }

    @Test
    public void test() {
        given().queryParam("v", "A").expect().statusCode(200).body(equalTo("A")).when().get(uri("/a"));
        given().queryParam("v", "B").expect().statusCode(200).body(equalTo("B")).when().get(uri("/a"));
        given().queryParam("v", "C").expect().statusCode(200).body(equalTo("C")).when().get(uri("/a"));
        given().queryParam("v", "X").expect().statusCode(404).when().get(uri("/a"));
    }

}
