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

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Titanite.Responses.ok;
import static org.nosceon.titanite.Titanite.serveFile;

/**
 * @author Johan Siebens
 */
public class HeadMethodTest extends AbstractE2ETest {

    private static final String TEXT = "hello world";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        File base = temporaryFolder.newFolder();
        File txt = new File(base, "temporary.txt");
        Files.write(TEXT, txt, Charsets.UTF_8);

        return server
            .register(Method.GET, "/text", req -> ok().text("hello world").toFuture())
            .register(Method.GET, "/streamA", req -> ok().body(new ByteArrayInputStream(TEXT.getBytes())).toFuture())
            .register(Method.GET, "/streamB", req -> ok().body(o -> ByteStreams.copy(new ByteArrayInputStream(TEXT.getBytes()), o)).toFuture())
            .register(Method.GET, "/file", req -> serveFile(req, txt).toFuture())
            .register(Method.GET, "/chunks",
                (req) -> ok().chunks(o -> {
                    o.write(TEXT.getBytes());
                    o.close();
                }).toFuture()
            )
            .register(Method.HEAD, "/head", req -> ok().header("Content-Length", "45").toFuture())
            .register(Method.POST, "/post", req -> ok().body("ok").toFuture())
            .start();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).header("Content-Length", equalTo("11")).body(equalTo("")).when().head(uri("/text"));
        given().expect().statusCode(200).header("Transfer-Encoding", equalTo("chunked")).body(equalTo("")).when().head(uri("/streamA"));
        given().expect().statusCode(200).header("Transfer-Encoding", equalTo("chunked")).body(equalTo("")).when().head(uri("/streamB"));
        given().expect().statusCode(200).header("Content-Length", equalTo("11")).body(equalTo("")).when().head(uri("/file"));
        given().expect().statusCode(200).header("Transfer-Encoding", equalTo("chunked")).body(equalTo("")).when().head(uri("/chunks"));

        given().expect().statusCode(200).header("Content-Length", equalTo("45")).body(equalTo("")).when().head(uri("/head"));

        given().expect().statusCode(405).when().head(uri("/post"));
    }

}
