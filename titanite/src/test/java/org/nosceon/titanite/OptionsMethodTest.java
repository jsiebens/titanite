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
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({OptionsMethodTest.DefaultFallbackTest.class, OptionsMethodTest.OptionsWithHandlerTest.class})
public class OptionsMethodTest {

    public static class DefaultFallbackTest extends AbstractE2ETest {

        @Override
        protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
            return server
                .register(Method.GET, "/a/:id", req -> ok().toFuture())
                .register(Method.POST, "/a/*path", req -> ok().toFuture())
                .start();
        }

        @Test
        public void test() {
            given().expect().statusCode(200).header("Allow", equalTo("GET, POST")).when().options(uri("/a/1"));
        }

    }

    public static class OptionsWithHandlerTest extends AbstractE2ETest {

        @Override
        protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
            return server
                .register(Method.GET, "/a/:id", req -> ok().toFuture())
                .register(Method.POST, "/a/*path", req -> ok().toFuture())
                .register(Method.OPTIONS, "/*path", req -> ok().header("Access-Control-Allow-Origin", "*").toFuture())
                .start();
        }

        @Test
        public void test() {
            given().expect().statusCode(200)
                .header("Allow", equalTo("GET, OPTIONS, POST"))
                .header("Access-Control-Allow-Origin", equalTo("*"))
                .when().options(uri("/a/1"));
        }

    }

}
