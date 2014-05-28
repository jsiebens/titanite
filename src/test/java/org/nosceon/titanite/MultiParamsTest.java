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

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

/**
 * @author Johan Siebens
 */
public class MultiParamsTest {

    @Test
    public void testGetShorts() {
        assertThat(params("a", "3", "7").getShorts("a"), contains((short) 3, (short) 7));
    }

    @Test
    public void testGetInts() {
        assertThat(params("a", "3", "7").getInts("a"), contains(3, 7));
    }

    @Test
    public void testGetLongs() {
        assertThat(params("a", "3", "7").getLongs("a"), contains(3l, 7l));
    }

    @Test
    public void testGetFloats() {
        assertThat(params("a", "3", "7").getFloats("a"), contains(3f, 7f));
    }

    @Test
    public void testGetDoubles() {
        assertThat(params("a", "3", "7").getDoubles("a"), contains(3d, 7d));
    }

    @Test
    public void testGetBooleans() {
        assertThat(params("a", "false", "true", "false").getBooleans("a"), contains(false, true, false));
    }

    private MultiParams params(String key, String... values) {
        return name -> key.equals(name) ? Arrays.asList(values) : Collections.emptyList();
    }

}
