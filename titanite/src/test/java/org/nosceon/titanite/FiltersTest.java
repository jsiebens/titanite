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
import static org.nosceon.titanite.Method.POST;
import static org.nosceon.titanite.Titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class FiltersTest extends AbstractE2ETest {

    private static final Filter GLOBAL_FILTER = (r, f) -> {
        if (r.queryParams().getBoolean("global", false)) {
            return ok().text("global").toFuture();
        }
        else {
            return f.apply(r);
        }
    };

    private static final Filter FILTER = (r, f) -> {
        if (r.queryParams().getBoolean("filtered", false)) {
            return ok().text("filtered").toFuture();
        }
        else {
            return f.apply(r);
        }
    };

    public static class TextController extends Controller {

        {
            get("/controller", r -> ok().text("controller").toFuture());
        }

    }

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .setFilter(GLOBAL_FILTER)
                .register(FILTER.andThen(new TextController()))
                .register(POST, "/resource", FILTER.andThen(r -> ok().text("resource").toFuture()))
                .start();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo("controller")).when().get(uri("/controller"));
        given().expect().statusCode(200).body(equalTo("resource")).when().post(uri("/resource"));

        given().queryParam("filtered", "true").expect().statusCode(200).body(equalTo("filtered")).when().get(uri("/controller"));
        given().queryParam("filtered", "true").expect().statusCode(200).body(equalTo("filtered")).when().post(uri("/resource"));

        given().queryParam("global", "true").expect().statusCode(200).body(equalTo("global")).when().get(uri("/controller"));
        given().queryParam("global", "true").expect().statusCode(200).body(equalTo("global")).when().post(uri("/resource"));
    }

}
