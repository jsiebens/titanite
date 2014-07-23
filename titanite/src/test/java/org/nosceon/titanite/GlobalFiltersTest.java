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

import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Test;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Titanite.$;
import static org.nosceon.titanite.Response.ok;
import static org.nosceon.titanite.Response.status;

/**
 * @author Johan Siebens
 */
public class GlobalFiltersTest extends AbstractE2ETest {

    private static final BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> SECURITY = (req, f) -> {
        String s = ofNullable(req.headers().getString(HttpHeaders.Names.AUTHORIZATION)).orElse("");
        if ("admin".equals(s)) {
            return f.apply(req).thenCompose(resp -> resp.header("x-titanite-a", "lorem").toFuture());
        }
        else {
            return status(401).toFuture();
        }
    };

    private static final BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> CONTENT_TYPE_JSON = (req, f) -> {
        String s = ofNullable(req.headers().getString(HttpHeaders.Names.CONTENT_TYPE)).orElse("");
        if ("application/json".equals(s)) {
            return f.apply(req).thenCompose(resp -> resp.header("x-titanite-b", "ipsum").toFuture());
        }
        else {
            return status(415).toFuture();
        }
    };

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .setFilter($(SECURITY, CONTENT_TYPE_JSON))
                .register(GET, "/resource", (r) -> ok().body("hello").toFuture())
                .start();
    }

    @Test
    public void test() {
        given()
            .expect()
            .statusCode(401).when().get(uri("/resource"));

        given()
            .header(HttpHeaders.Names.AUTHORIZATION, "admin")
            .expect()
            .header("x-titanite-a", "lorem")
            .statusCode(415).when().get(uri("/resource"));

        given()
            .header(HttpHeaders.Names.AUTHORIZATION, "admin")
            .header(HttpHeaders.Names.CONTENT_TYPE, "application/json")
            .expect()
            .header("x-titanite-a", "lorem")
            .header("x-titanite-b", "ipsum")
            .statusCode(200).when().get(uri("/resource"));
    }

}
