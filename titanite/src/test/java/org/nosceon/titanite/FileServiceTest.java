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
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Titanite.fileService;
import static org.nosceon.titanite.Titanite.serveFile;

/**
 * @author Johan Siebens
 */
public class FileServiceTest extends AbstractE2ETest {

    private static final String TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        File base = temporaryFolder.newFolder();

        File file = new File(base, "forbidden.txt");
        Files.write(TEXT, file, Charsets.UTF_8);

        File docRoot = new File(base, "docRoot");
        docRoot.mkdirs();

        File txt = new File(docRoot, "temporary.txt");
        Files.write(TEXT, txt, Charsets.UTF_8);

        return
            server
                .register(Method.GET, "/lorem.txt", req -> serveFile(req, txt).toFuture())
                .register(Method.GET, "/a/b/c/*mycustompath", fileService(docRoot, req -> req.pathParams().getString("mycustompath")))
                .register(Method.GET, "/*path", fileService(docRoot))
                .start();
    }

    @Test
    public void test() throws IOException {
        given().expect().statusCode(200).body(equalTo(TEXT)).when().get(uri("/lorem.txt"));
        given().expect().statusCode(200).body(equalTo(TEXT)).when().get(uri("/temporary.txt"));
        given().expect().statusCode(200).body(equalTo(TEXT)).when().get(uri("/a/b/c/temporary.txt"));
        given().expect().statusCode(403).when().get(uri("/../forbidden.txt"));
    }

}
