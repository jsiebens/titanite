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
public class QueryParamsTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer(port)
                .register(GET, "/a", (r) -> Responses.ok().body(r.queryParams.get("p")).toFuture())
                .register(GET, "/b", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getShort("p"))).toFuture())
                .register(GET, "/c", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getInt("p"))).toFuture())
                .register(GET, "/d", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getLong("p"))).toFuture())
                .register(GET, "/e", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getFloat("p"))).toFuture())
                .register(GET, "/f", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getDouble("p"))).toFuture())
                .register(GET, "/g", (r) -> Responses.ok().body(String.valueOf(r.queryParams.getBoolean("p"))).toFuture())
                .start();
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().queryParam("p", "apple").expect().statusCode(200).body(equalTo("apple")).when().get(uri(port, "/a"));
        given().queryParam("p", "10").expect().statusCode(200).body(equalTo("10")).when().get(uri(port, "/b"));
        given().queryParam("p", "20").expect().statusCode(200).body(equalTo("20")).when().get(uri(port, "/c"));
        given().queryParam("p", "30").expect().statusCode(200).body(equalTo("30")).when().get(uri(port, "/d"));
        given().queryParam("p", "40").expect().statusCode(200).body(equalTo("40.0")).when().get(uri(port, "/e"));
        given().queryParam("p", "50").expect().statusCode(200).body(equalTo("50.0")).when().get(uri(port, "/f"));
        given().queryParam("p", "true").expect().statusCode(200).body(equalTo("true")).when().get(uri(port, "/g"));
    }

}
