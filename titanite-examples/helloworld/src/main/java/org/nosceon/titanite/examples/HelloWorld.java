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
package org.nosceon.titanite.examples;

import org.nosceon.titanite.HttpServer;

import static org.nosceon.titanite.Method.GET;
import static org.nosceon.titanite.Method.POST;
import static org.nosceon.titanite.Response.ok;
import static org.nosceon.titanite.service.ResourceService.webJarResourceService;
import static org.nosceon.titanite.view.MustacheViewRenderer.render;

/**
 * @author Johan Siebens
 */
public class HelloWorld {

    public static void main(String[] args) {

        new HttpServer()
            .register(GET, "/",
                req -> ok().body(render("index")).toFuture()
            )
            .register(POST, "/hello",
                req -> {
                    String name = req.body().asForm().getString("name", "Stranger");
                    String color = req.body().asForm().getString("color", "black");
                    return ok().body(render(new HelloView(name, color))).toFuture();
                }
            )
            .register(GET, "/*path", webJarResourceService())
            .start();

    }

}
