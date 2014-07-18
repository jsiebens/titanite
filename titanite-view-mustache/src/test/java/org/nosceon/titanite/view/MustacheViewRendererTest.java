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
package org.nosceon.titanite.view;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author Johan Siebens
 */
public class MustacheViewRendererTest {

    public static class HelloView extends View {

        private String name;

        public HelloView(String name) {
            super("hello");
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    @View.Template("hello.mustache")
    public static class Hello {

        private String name;

        public Hello(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    @Test
    public void testRenderViewByName() throws Exception {
        assertThat(render("index"), equalTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit."));
    }

    @Test
    public void testView() throws Exception {
        assertThat(render(new HelloView("World")), equalTo("Hello World"));
    }

    @Test
    public void testWithAnnotatedView() throws Exception {
        assertThat(render(new Hello("World")), equalTo("Hello World"));
    }

    private String render(Object o) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            MustacheViewRenderer.render(o).writeTo(out);
            return new String(out.toByteArray());
        }
    }

}
