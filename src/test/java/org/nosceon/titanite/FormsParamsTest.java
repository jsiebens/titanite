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
import static org.nosceon.titanite.Method.POST;

/**
 * @author Johan Siebens
 */
public class FormsParamsTest extends AbstractE2ETest {

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(POST, "/a", (r) -> Titanite.Responses.ok().body(r.body.asForm().getString("p")).toFuture())
                .register(POST, "/b", (r) -> Titanite.Responses.ok().body(String.valueOf(r.body.asForm().getShort("p"))).toFuture())
                .register(POST, "/c", (r) -> Titanite.Responses.ok().body(String.valueOf(r.body.asForm().getInt("p"))).toFuture())
                .register(POST, "/d", (r) -> Titanite.Responses.ok().body(String.valueOf(r.body.asForm().getLong("p"))).toFuture())
                .register(POST, "/e", (r) -> Titanite.Responses.ok().body(String.valueOf(r.body.asForm().getFloat("p"))).toFuture())
                .register(POST, "/f", (r) -> Titanite.Responses.ok().body(String.valueOf(r.body.asForm().getDouble("p"))).toFuture())
                .register(POST, "/g", (r) -> Titanite.Responses.ok().body(String.valueOf(r.body.asForm().getBoolean("p"))).toFuture())

                .register(POST, "/ma", (r) -> Titanite.Responses.ok().body(String.valueOf(r.body.asForm().getStrings("p"))).toFuture())

                .start();
    }

    @Test
    public void test() {
        given().formParam("p", "apple").expect().statusCode(200).body(equalTo("apple")).when().post(uri("/a"));
        given().formParam("p", "10").expect().statusCode(200).body(equalTo("10")).when().post(uri("/b"));
        given().formParam("p", "20").expect().statusCode(200).body(equalTo("20")).when().post(uri("/c"));
        given().formParam("p", "30").expect().statusCode(200).body(equalTo("30")).when().post(uri("/d"));
        given().formParam("p", "40").expect().statusCode(200).body(equalTo("40.0")).when().post(uri("/e"));
        given().formParam("p", "50").expect().statusCode(200).body(equalTo("50.0")).when().post(uri("/f"));
        given().formParam("p", "true").expect().statusCode(200).body(equalTo("true")).when().post(uri("/g"));

        given().formParam("p", "apple").formParam("p", "orange").expect().statusCode(200).body(equalTo("[apple, orange]")).when().post(uri("/ma"));
    }

}
