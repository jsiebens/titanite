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
package org.nosceon.titanite.json;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.nosceon.titanite.json.GsonMapper.json;

/**
 * @author Johan Siebens
 */
public class GsonMapperTest {

    public static class Hello {

        private String name;

        public Hello() {
        }

        public Hello(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @Test
    public void testToJson() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        json(new Hello("world")).writeTo(out);
        String s = new String(out.toByteArray());

        assertThat(s, equalTo("{\"name\":\"world\"}"));
    }

    @Test
    public void testFromJson() throws Exception {
        String json = "{\"name\":\"world\"}";
        Hello hello = json(Hello.class).readFrom(new ByteArrayInputStream(json.getBytes()));
        assertThat(hello.getName(), equalTo("world"));
    }

}
