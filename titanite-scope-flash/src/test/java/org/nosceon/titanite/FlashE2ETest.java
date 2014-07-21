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
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Titanite.Responses.ok;
import static org.nosceon.titanite.scope.Flash.enableFlash;
import static org.nosceon.titanite.scope.Flash.flash;
import static org.nosceon.titanite.scope.FlashFilter.DEFAULT_FLASH_COOKIE_NAME;

/**
* @author Johan Siebens
*/
public class FlashE2ETest extends AbstractE2ETest {

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) {
        return
            server
                .setFilter(enableFlash())
                .register(GET, "/a", (r) -> {
                    flash(r).set("name", "titanite");
                    flash(r).set("lorem", "ipsum");
                    return ok().toFuture();
                })
                .register(GET, "/b",
                    req -> {
                        int count = flash(req).getInt("count", 0);
                        flash(req).set("count", count + 1);
                        return ok().text(String.valueOf(count)).toFuture();
                    }
                )
                .start();
    }

    @Test
    public void testA() {
        given().expect().statusCode(200).cookie(DEFAULT_FLASH_COOKIE_NAME, "\"lorem=ipsum&name=titanite\"").when().get(uri("/a"));
    }

    @Test
    public void testB() {
        given()
            .cookie(DEFAULT_FLASH_COOKIE_NAME, "\"count=1\"")
            .expect().statusCode(200).cookie(DEFAULT_FLASH_COOKIE_NAME, "\"count=2\"").when().get(uri("/b"));
    }

    @Test
    public void testC() {
        given()
            .cookie(DEFAULT_FLASH_COOKIE_NAME, "\"lorem=ipsum\"")
            .expect().statusCode(200).cookie(DEFAULT_FLASH_COOKIE_NAME, "\"count=1\"").when().get(uri("/b"));
    }

}
