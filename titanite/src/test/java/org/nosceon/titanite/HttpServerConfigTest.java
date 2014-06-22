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
import static org.junit.Assert.assertThat;

/**
 * @author Johan Siebens
 */
public class HttpServerConfigTest {

    @Test
    public void testDefaultValues() {
        HttpServerConfig config = new HttpServerConfig.Default();
        assertThat(config.getPort(), equalTo(HttpServerConfig.DEFAULT_PORT));
        assertThat(config.getIoWorkerCount(), equalTo(HttpServerConfig.DEFAULT_IO_WORKER_COUNT));
        assertThat(config.getMaxRequestSize(), equalTo(HttpServerConfig.DEFAULT_MAX_REQUEST_SIZE));
    }

    @Test
    public void testFallbackToSystemProperties() {
        System.setProperty("titanite.port", "3");
        System.setProperty("titanite.io-worker-count", "5");
        System.setProperty("titanite.max-request-size", "7");

        HttpServerConfig config = new HttpServerConfig.Default();
        assertThat(config.getPort(), equalTo(3));
        assertThat(config.getIoWorkerCount(), equalTo(5));
        assertThat(config.getMaxRequestSize(), equalTo(7l));
    }

}
