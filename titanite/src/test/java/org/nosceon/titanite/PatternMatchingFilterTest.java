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
import static org.nosceon.titanite.Filters.filter;
import static org.nosceon.titanite.Response.ok;

/**
 * @author Johan Siebens
 */
public class PatternMatchingFilterTest extends AbstractE2ETest {

    private Filter filterA = (request, requestCompletionStageFunction) -> ok().text("filter").toFuture();

    private Function<Request, CompletionStage<Response>> handler = request -> ok().text("handler").toFuture();

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .setFilter(
                    filter(filterA)
                        .include("/static/*.txt")
                        .include("/static/**/*.html")
                        .exclude("/static/h?llo.txt")
                        .exclude("/static/index.html")
                )
                .register(Method.GET, "/*path", handler)
                .start();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo("filter")).when().get(uri("/static/resource.txt"));
        given().expect().statusCode(200).body(equalTo("filter")).when().get(uri("/static/blog/index.html"));

        given().expect().statusCode(200).body(equalTo("handler")).when().get(uri("/static/hello.txt"));
        given().expect().statusCode(200).body(equalTo("handler")).when().get(uri("/static/index.html"));
        given().expect().statusCode(200).body(equalTo("handler")).when().get(uri("/static/style/style.css"));
    }

}
