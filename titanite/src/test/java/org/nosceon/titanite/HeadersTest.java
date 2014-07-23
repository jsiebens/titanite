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

import com.google.common.net.HttpHeaders;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Response.ok;

/**
 * @author Johan Siebens
 */
public class HeadersTest extends AbstractE2ETest {

    private static final Date DATE = new Date(5000);

    private static final Locale LOCALE = Locale.ITALY;

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(GET, "/a", (r) -> ok().body(r.headers().getString("p")).header("m", r.headers().getString("p")).toFuture())
                .register(GET, "/b", (r) -> ok().body(String.valueOf(r.headers().getShort("p"))).toFuture())
                .register(GET, "/c", (r) -> ok().body(String.valueOf(r.headers().getInt("p"))).toFuture())
                .register(GET, "/d", (r) -> ok().body(String.valueOf(r.headers().getLong("p"))).toFuture())
                .register(GET, "/e", (r) -> ok().body(String.valueOf(r.headers().getFloat("p"))).toFuture())
                .register(GET, "/f", (r) -> ok().body(String.valueOf(r.headers().getDouble("p"))).toFuture())
                .register(GET, "/g", (r) -> ok().body(String.valueOf(r.headers().getBoolean("p"))).toFuture())
                .register(GET, "/headers", (r) ->
                        ok()
                            .type(MediaType.APPLICATION_XML)
                            .language(LOCALE)
                            .lastModified(DATE)
                            .location(uri("/location"))
                            .toFuture()
                )

                .register(GET, "/ma", (r) -> ok().body(String.valueOf(r.headers().getStrings("p"))).toFuture())

                .start();
    }

    @Test
    public void test() {
        given().header("p", "apple").expect().statusCode(200).header("m", "apple").body(equalTo("apple")).when().get(uri("/a"));
        given().header("p", "10").expect().statusCode(200).body(equalTo("10")).when().get(uri("/b"));
        given().header("p", "20").expect().statusCode(200).body(equalTo("20")).when().get(uri("/c"));
        given().header("p", "30").expect().statusCode(200).body(equalTo("30")).when().get(uri("/d"));
        given().header("p", "40").expect().statusCode(200).body(equalTo("40.0")).when().get(uri("/e"));
        given().header("p", "50").expect().statusCode(200).body(equalTo("50.0")).when().get(uri("/f"));
        given().header("p", "true").expect().statusCode(200).body(equalTo("true")).when().get(uri("/g"));

        given()
            .expect()
            .statusCode(200)
            .header(HttpHeaders.CONTENT_TYPE, equalTo("application/xml"))
            .header(HttpHeaders.LAST_MODIFIED, equalTo("Thu, 01 Jan 1970 00:00:05 GMT"))
            .header(HttpHeaders.CONTENT_LANGUAGE, equalTo("it_IT"))
            .header(HttpHeaders.LOCATION, equalTo(uri("/location")))
            .when().get(uri("/headers"));

        given().header("p", "apple").header("p", "orange").expect().statusCode(200).body(equalTo("[apple, orange]")).when().get(uri("/ma"));
    }

}
