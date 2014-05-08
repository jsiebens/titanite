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

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class QueryParamsTest extends AbstractE2ETest {

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(GET, "/a", (r) -> ok().body(r.queryParams.getString("p")).toFuture())
                .register(GET, "/b", (r) -> ok().body(String.valueOf(r.queryParams.getShort("p"))).toFuture())
                .register(GET, "/c", (r) -> ok().body(String.valueOf(r.queryParams.getInt("p"))).toFuture())
                .register(GET, "/d", (r) -> ok().body(String.valueOf(r.queryParams.getLong("p"))).toFuture())
                .register(GET, "/e", (r) -> ok().body(String.valueOf(r.queryParams.getFloat("p"))).toFuture())
                .register(GET, "/f", (r) -> ok().body(String.valueOf(r.queryParams.getDouble("p"))).toFuture())
                .register(GET, "/g", (r) -> ok().body(String.valueOf(r.queryParams.getBoolean("p"))).toFuture())

                .register(GET, "/ma", (r) -> ok().body(String.valueOf(r.queryParams.getStrings("p"))).toFuture())

                .start();
    }

    @Test
    public void test() {
        given().queryParam("p", "apple").expect().statusCode(200).body(equalTo("apple")).when().get(uri("/a"));
        given().queryParam("p", "10").expect().statusCode(200).body(equalTo("10")).when().get(uri("/b"));
        given().queryParam("p", "20").expect().statusCode(200).body(equalTo("20")).when().get(uri("/c"));
        given().queryParam("p", "30").expect().statusCode(200).body(equalTo("30")).when().get(uri("/d"));
        given().queryParam("p", "40").expect().statusCode(200).body(equalTo("40.0")).when().get(uri("/e"));
        given().queryParam("p", "50").expect().statusCode(200).body(equalTo("50.0")).when().get(uri("/f"));
        given().queryParam("p", "true").expect().statusCode(200).body(equalTo("true")).when().get(uri("/g"));

        given().queryParam("p", "apple").queryParam("p", "orange").expect().statusCode(200).body(equalTo("[apple, orange]")).when().get(uri("/ma"));
    }

}
