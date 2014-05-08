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

import com.google.common.io.ByteStreams;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public class BodyWriterResponseTest extends AbstractE2ETest {

    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(GET, "/stream", (r) -> ok().body(new ByteArrayInputStream(TEXT.getBytes())).toFuture())
                .register(GET, "/resource", (r) -> ok().body(o -> ByteStreams.copy(new ByteArrayInputStream(TEXT.getBytes()), o)).toFuture())
                .start();
    }

    @Test
    public void test() {
        given()
            .expect()
            .header(HttpHeaders.Names.TRANSFER_ENCODING, "chunked")
            .statusCode(200).body(equalTo(TEXT)).when().get(uri("/stream"));

        given()
            .expect()
            .header(HttpHeaders.Names.TRANSFER_ENCODING, "chunked")
            .statusCode(200).body(equalTo(TEXT)).when().get(uri("/resource"));
    }

}
