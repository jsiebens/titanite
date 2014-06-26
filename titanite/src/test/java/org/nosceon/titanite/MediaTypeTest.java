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
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.nosceon.titanite.MediaType.*;

/**
 * @author Johan Siebens
 */
public class MediaTypeTest {

    @Test
    public void test() {
        List<MediaType> candidates = Arrays.asList(APPLICATION_JSON, APPLICATION_XML, TEXT_PLAIN);

        assertThat(bestCandidate(valuesOf("*/*"), candidates), equalTo(APPLICATION_JSON));
        assertThat(bestCandidate(valuesOf("application/xml"), candidates), equalTo(APPLICATION_XML));
        assertThat(bestCandidate(valuesOf("application/*;q=0.4, text/*;q=0.5"), candidates), equalTo(TEXT_PLAIN));
        assertThat(bestCandidate(valuesOf("application/*, text/*;q=0.5"), candidates), equalTo(APPLICATION_JSON));
        assertThat(bestCandidate(valuesOf("text/html"), candidates), nullValue());
    }

}
