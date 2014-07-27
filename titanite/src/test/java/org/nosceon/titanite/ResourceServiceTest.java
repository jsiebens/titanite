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
import org.nosceon.titanite.service.ResourceService;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.CompositeHandler.h;
import static org.nosceon.titanite.service.ResourceService.publicResourceService;
import static org.nosceon.titanite.service.ResourceService.serveResource;
import static org.nosceon.titanite.service.ResourceService.webJarResourceService;

/**
 * @author Johan Siebens
 */
public class ResourceServiceTest extends AbstractE2ETest {

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(Method.GET, "/lorem.txt", req -> serveResource(req, "/public/hello.txt").toFuture())
                .register(Method.GET, "/a/b/c/*mycustompath", publicResourceService(req -> req.pathParams().getString("mycustompath")))
                .register(Method.GET, "/*path", h(publicResourceService(), webJarResourceService()))
                .start();
    }

    @Test
    public void testA() {
        given().expect().statusCode(200).body(equalTo("hello 1 from public")).when().get(uri("/a/b/c/hello1.txt"));
        given().expect().statusCode(403).when().get(uri("/../public/hello1.txt"));
        given().expect().statusCode(200).body(equalTo("hello 2 from webjars")).when().get(uri("/hello2.txt"));
        given().expect().statusCode(404).when().get(uri("/hello3.txt"));
        given().expect().statusCode(200).body(equalTo("hello 1 from public")).when().get(uri("/hello1.txt"));

        given().expect().statusCode(404).when().get(uri("/a"));
        given().expect().statusCode(200).body(equalTo("hello 1 from public/b")).when().get(uri("/b"));
    }

    @Test
    public void testB() {
        given().expect().statusCode(200).when().get(uri("/jquery/1.9.0/jquery.js"));
    }

    @Test
    public void testC() {
        given().expect().statusCode(404).when().get(uri("/jquery/1.9.0"));
    }

}
