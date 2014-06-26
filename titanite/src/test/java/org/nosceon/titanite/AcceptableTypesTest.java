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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.MediaType.*;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Titanite.Responses.notAcceptable;
import static org.nosceon.titanite.Titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class AcceptableTypesTest extends AbstractE2ETest {

    private static List<MediaType> candidates = Arrays.asList(APPLICATION_JSON, APPLICATION_XML, TEXT_PLAIN);

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(GET, "/a", (r) ->
                    Optional.ofNullable(r.acceptableType(candidates))
                        .map(m -> ok().body(m.toString()).toFuture())
                        .orElseGet(() -> notAcceptable().toFuture()))
                .start();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo(APPLICATION_JSON.toString())).when().get(uri("/a"));
        given().header(HttpHeaders.ACCEPT, "*/*").expect().statusCode(200).body(equalTo(APPLICATION_JSON.toString())).when().get(uri("/a"));
        given().header(HttpHeaders.ACCEPT, "application/xml").expect().statusCode(200).body(equalTo(APPLICATION_XML.toString())).when().get(uri("/a"));
        given().header(HttpHeaders.ACCEPT, "application/*;q=0.4, text/*;q=0.5").expect().statusCode(200).body(equalTo(TEXT_PLAIN.toString())).when().get(uri("/a"));
        given().header(HttpHeaders.ACCEPT, "text/html").expect().statusCode(406).when().get(uri("/a"));
    }

}
