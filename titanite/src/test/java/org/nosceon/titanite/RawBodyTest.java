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

import java.io.InputStream;
import java.util.Scanner;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.POST;
import static org.nosceon.titanite.Response.ok;

/**
 * @author Johan Siebens
 */
public class RawBodyTest extends AbstractE2ETest {

    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(POST, "/text", req -> ok().body(req.body().asText()).toFuture())
                .register(POST, "/stream", req -> ok().body(convertStreamToString(req.body().asStream())).toFuture())
                .register(POST, "/reader", req -> ok().body(req.body().as(new StringBodyReader())).toFuture())
                .register(POST, "/unsupported", req -> {
                    req.body().as(RawBodyTest.class);
                    return ok().body("ok").toFuture();
                })
                .start();
    }

    @Test
    public void test() {
        given().body(TEXT).expect().statusCode(200).body(equalTo(TEXT)).when().post(uri("/text"));
        given().body(TEXT).expect().statusCode(200).body(equalTo(TEXT)).when().post(uri("/stream"));
        given().body(TEXT).expect().statusCode(200).body(equalTo(TEXT)).when().post(uri("/reader"));
        given().body(TEXT).expect().statusCode(500).when().post(uri("/unsupported"));
    }

    private static String convertStreamToString(InputStream is) {
        Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private static class StringBodyReader implements BodyReader<String> {

        @Override
        public String readFrom(InputStream in) throws Exception {
            return convertStreamToString(in);
        }

    }

}
