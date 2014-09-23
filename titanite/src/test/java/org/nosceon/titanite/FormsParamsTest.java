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
import org.nosceon.titanite.body.FormParams;

import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.POST;
import static org.nosceon.titanite.Response.ok;

/**
 * @author Johan Siebens
 */
public class FormsParamsTest extends AbstractMultiE2ETest {

    public FormsParamsTest(boolean secure) {
        super(secure);
    }

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(POST, "/a", req -> ok().body(req.body().asForm().getString("p")).toFuture())
                .register(POST, "/b", req -> ok().body(String.valueOf(req.body().asForm().getShort("p"))).toFuture())
                .register(POST, "/c", req -> ok().body(String.valueOf(req.body().asForm().getInt("p"))).toFuture())
                .register(POST, "/d", req -> ok().body(String.valueOf(req.body().asForm().getLong("p"))).toFuture())
                .register(POST, "/e", req -> ok().body(String.valueOf(req.body().asForm().getFloat("p"))).toFuture())
                .register(POST, "/f", req -> ok().body(String.valueOf(req.body().asForm().getDouble("p"))).toFuture())
                .register(POST, "/g", req -> ok().body(String.valueOf(req.body().asForm().getBoolean("p"))).toFuture())

                .register(POST, "/ma", req -> ok().body(String.valueOf(req.body().as(MultiParams.class).getStrings("p"))).toFuture())
                .register(POST, "/mb", req -> {
                    FormParams form = req.body().asForm();
                    List<String> p = form.getStrings("p");
                    return ok().text(String.valueOf(p.size())).toFuture();
                })

                .register(POST, "/unsupported", req -> ok().body(req.body().as(String.class)).toFuture())


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
        given().formParam("p", "apple").formParam("p", "orange").expect().statusCode(200).body(equalTo("2")).when().post(uri("/mb"));
        given().formParam("a", "b").expect().statusCode(200).body(equalTo("0")).when().post(uri("/mb"));

        given().formParam("p", "true").expect().statusCode(500).when().post(uri("/unsupported"));
    }

}
