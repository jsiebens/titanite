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

/**
 * @author Johan Siebens
 */
public final class HttpServerConfig implements HttpServer.Config {

    private int port;

    private int ioWorkerCount;

    private long maxRequestSize;

    public HttpServerConfig() {
        this(8080, Titanite.DEFAULT_IO_WORKER_COUNT, Titanite.DEFAULT_MAX_REQUEST_SIZE);
    }

    public HttpServerConfig(int port, int ioWorkerCount, long maxRequestSize) {
        this.port = port;
        this.ioWorkerCount = ioWorkerCount;
        this.maxRequestSize = maxRequestSize;
    }

    public HttpServerConfig port(int port) {
        this.port = port;
        return this;
    }

    public HttpServerConfig ioWorkerCount(int ioWorkerCount) {
        this.ioWorkerCount = ioWorkerCount;
        return this;
    }

    public HttpServerConfig maxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
        return this;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getIoWorkerCount() {
        return ioWorkerCount;
    }

    @Override
    public long getMaxRequestSize() {
        return maxRequestSize;
    }

}
