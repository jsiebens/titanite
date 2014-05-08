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
public class PathParamsTest extends AbstractE2ETest {

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(GET, "/a/{p}", (r) -> ok().body(r.pathParams.getString("p")).toFuture())
                .register(GET, "/b/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getShort("p"))).toFuture())
                .register(GET, "/c/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getInt("p"))).toFuture())
                .register(GET, "/d/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getLong("p"))).toFuture())
                .register(GET, "/e/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getFloat("p"))).toFuture())
                .register(GET, "/f/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getDouble("p"))).toFuture())
                .register(GET, "/g/{p}", (r) -> ok().body(String.valueOf(r.pathParams.getBoolean("p"))).toFuture())
                .start();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo("apple")).when().get(uri("/a/apple"));
        given().expect().statusCode(200).body(equalTo("10")).when().get(uri("/b/10"));
        given().expect().statusCode(200).body(equalTo("20")).when().get(uri("/c/20"));
        given().expect().statusCode(200).body(equalTo("30")).when().get(uri("/d/30"));
        given().expect().statusCode(200).body(equalTo("40.0")).when().get(uri("/e/40"));
        given().expect().statusCode(200).body(equalTo("50.0")).when().get(uri("/f/50"));
        given().expect().statusCode(200).body(equalTo("true")).when().get(uri("/g/true"));
    }

}
