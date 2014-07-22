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

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Titanite.$;
import static org.nosceon.titanite.Titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class AttributesTest extends AbstractE2ETest {

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .setFilter(
                    $(
                        (req, handler) -> handler.apply(req.withAttribute("number", "42")),
                        (req, handler) -> handler.apply(req.withAttribute("lorem", "ipsum"))
                    )
                )
                .register(GET, "/a", (r) -> ok().text(r.attributes().get("number") + " " + r.attributes().get("lorem")).toFuture())
                .start();
    }

    @Test
    public void test() {
        given()
            .expect()
            .statusCode(200).body(equalTo("42 ipsum")).when().get(uri("/a"));
    }

}
