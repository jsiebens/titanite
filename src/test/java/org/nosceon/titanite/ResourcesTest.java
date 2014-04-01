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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.nosceon.titanite.Titanite.*;

/**
 * @author Johan Siebens
 */
public class ResourcesTest extends AbstractE2ETest {

    private Shutdownable shutdownable;

    private int port;

    @Before
    public void setUp() {
        port = findFreePort();
        shutdownable =
            newServer()
                .register(Method.GET, "/a", (r) -> ok().toFuture())
                .notFound(
                    compose(
                        sync(PUBLIC_RESOURCES),
                        sync(WEBJAR_RESOURCES)
                    )
                )
                .start(port);
    }

    @After
    public void tearDown() {
        shutdownable.stop();
    }

    @Test
    public void test() {
        given().expect().statusCode(200).body(equalTo("hello 1 from public")).when().get(uri(port, "/hello1.txt"));
        given().expect().statusCode(403).when().get(uri(port, "/../public/hello1.txt"));
        given().expect().statusCode(200).body(equalTo("hello 2 from webjars")).when().get(uri(port, "/hello2.txt"));
        given().expect().statusCode(404).when().get(uri(port, "/hello3.txt"));
    }


}
