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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Johan Siebens
 */
public final class ImmutableSettings implements Settings {

    public static Builder newSettings() {
        return new Builder();
    }

    private final int ioWorkerCount;

    private final long maxRequestSize;

    private final long maxMultipartRequestSize;

    private final List<Connector> connectors;

    private ImmutableSettings(int ioWorkerCount, long maxRequestSize, long maxMultipartRequestSize, List<Connector> connectors) {
        this.ioWorkerCount = ioWorkerCount;
        this.maxRequestSize = maxRequestSize;
        this.maxMultipartRequestSize = maxMultipartRequestSize;
        this.connectors = Collections.unmodifiableList(new ArrayList<>(connectors));
    }

    @Override
    public int ioWorkerCount() {
        return ioWorkerCount;
    }

    @Override
    public long maxRequestSize() {
        return maxRequestSize;
    }

    @Override
    public long maxMultipartRequestSize() {
        return maxMultipartRequestSize;
    }

    @Override
    public List<Connector> connectors() {
        return connectors;
    }


    public static class Builder {

        private int ioWorkerCount = DEFAULT_IO_WORKER_COUNT;

        private long maxRequestSize = DEFAULT_MAX_REQUEST_SIZE;

        private long maxMultipartRequestSize = DEFAULT_MAX_MULTI_PART_REQUEST_SIZE;

        private List<Connector> connectors = new ArrayList<>();

        public Builder setIoWorkerCount(int ioWorkerCount) {
            this.ioWorkerCount = ioWorkerCount;
            return this;
        }

        public Builder setMaxRequestSize(long maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
            return this;
        }

        public Builder setMaxMultipartRequestSize(long maxMultipartRequestSize) {
            this.maxMultipartRequestSize = maxMultipartRequestSize;
            return this;
        }

        public Builder addHttpConnector(int port) {
            this.connectors.add(new ImmutableConnector(ConnectorType.HTTP, null, port, null, null, null));
            return this;
        }

        public Builder addHttpConnector(String address, int port) {
            this.connectors.add(new ImmutableConnector(ConnectorType.HTTP, address, port, null, null, null));
            return this;
        }

        public Builder addHttpsConnector(int port) {
            this.connectors.add(new ImmutableConnector(ConnectorType.HTTPS, null, port, null, null, null));
            return this;
        }

        public Builder addHttpsConnector(int port, File certificatePath, File keyPath) {
            this.connectors.add(new ImmutableConnector(ConnectorType.HTTPS, null, port, certificatePath, keyPath, null));
            return this;
        }

        public Builder addHttpsConnector(String address, int port, File certificatePath, File keyPath) {
            this.connectors.add(new ImmutableConnector(ConnectorType.HTTPS, address, port, certificatePath, keyPath, null));
            return this;
        }

        public Builder addHttpsConnector(int port, File certificatePath, File keyPath, String keyPassword) {
            this.connectors.add(new ImmutableConnector(ConnectorType.HTTPS, null, port, certificatePath, keyPath, keyPassword));
            return this;
        }

        public Builder addHttpsConnector(String address, int port, File certificatePath, File keyPath, String keyPassword) {
            this.connectors.add(new ImmutableConnector(ConnectorType.HTTPS, address, port, certificatePath, keyPath, keyPassword));
            return this;
        }

        public Settings build() {
            return new ImmutableSettings(ioWorkerCount, maxRequestSize, maxMultipartRequestSize, connectors);
        }

    }


    private static class ImmutableConnector implements Connector {

        private final ConnectorType type;

        private final String address;

        private final int port;

        private final File certificatePath;

        private final File keyPath;

        private final String keyPassword;

        private ImmutableConnector(ConnectorType type, String address, int port, File certificatePath, File keyPath, String keyPassword) {
            this.type = type;
            this.address = address;
            this.port = port;
            this.certificatePath = certificatePath;
            this.keyPath = keyPath;
            this.keyPassword = keyPassword;
        }

        @Override
        public ConnectorType type() {
            return type;
        }

        @Override
        public String address() {
            return address;
        }

        @Override
        public int port() {
            return port;
        }

        @Override
        public File certificatePath() {
            return certificatePath;
        }

        @Override
        public File keyPath() {
            return keyPath;
        }

        @Override
        public String keyPassword() {
            return keyPassword;
        }

    }

}

