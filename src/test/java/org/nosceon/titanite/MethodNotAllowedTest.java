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
import static org.nosceon.titanite.Method.DELETE;
import static org.nosceon.titanite.Method.GET;

/**
 * @author Johan Siebens
 */
public class MethodNotAllowedTest extends AbstractE2ETest {

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(GET, "/resource", (r) -> Titanite.Responses.ok().body(r.method().name()).toFuture())
                .register(DELETE, "/resource", (r) -> Titanite.Responses.ok().body(r.method().name()).toFuture())
                .start();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).when().get(uri("/resource"));
        given().expect().statusCode(405).when().post(uri("/resource"));
        given().expect().statusCode(405).when().put(uri("/resource"));
        given().expect().statusCode(200).when().delete(uri("/resource"));
        given().expect().statusCode(405).when().patch(uri("/resource"));
    }

}
