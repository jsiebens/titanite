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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Johan Siebens
 */
public class SingleParamsTest {

    @Test
    public void testGetShort() {
        assertThat(params("a", "1").getShort("a"), equalTo((short) 1));
        assertThat(params("a", "1").getShort("b"), nullValue());
        assertThat(params("a", "1").getShort("b", (short) 2), equalTo((short) 2));
    }

    @Test
    public void testGetInteger() {
        assertThat(params("a", "1").getInt("a"), equalTo(1));
        assertThat(params("a", "1").getInt("b"), nullValue());
        assertThat(params("a", "1").getInt("b", 2), equalTo(2));
    }

    @Test
    public void testGetLong() {
        assertThat(params("a", "1").getLong("a"), equalTo(1l));
        assertThat(params("a", "1").getLong("b"), nullValue());
        assertThat(params("a", "1").getLong("b", 2), equalTo(2l));
    }

    @Test
    public void testGetFloat() {
        assertThat(params("a", "1").getFloat("a"), equalTo(1f));
        assertThat(params("a", "1").getFloat("b"), nullValue());
        assertThat(params("a", "1").getFloat("b", 2), equalTo(2f));
    }

    @Test
    public void testGetDouble() {
        assertThat(params("a", "1").getDouble("a"), equalTo(1d));
        assertThat(params("a", "1").getDouble("b"), nullValue());
        assertThat(params("a", "1").getDouble("b", 2), equalTo(2d));
    }

    @Test
    public void testGetBoolean() {
        assertThat(params("a", "true").getBoolean("a"), is(true));
        assertThat(params("a", "true").getBoolean("b"), nullValue());
        assertThat(params("a", "true").getBoolean("b", false), is(false));
    }

    private SingleParams params(String key, String value) {
        return new SingleParams() {

            @Override
            public String getString(String name) {
                return name.equals(key) ? value : null;
            }

            @Override
            protected IllegalArgumentException translate(Exception e, String type, String name, String value) {
                return new IllegalArgumentException();
            }

        };
    }

}
