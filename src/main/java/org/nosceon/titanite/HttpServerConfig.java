package org.nosceon.titanite;

/**
 * @author Johan Siebens
 */
public interface HttpServerConfig {

    int getPort();

    int getIoWorkerCount();

    long getMaxRequestSize();


    final class Default implements HttpServerConfig {

        private int port;

        private int ioWorkerCount;

        private long maxRequestSize;

        public Default() {
            this(Titanite.DEFAULT_PORT, Titanite.DEFAULT_IO_WORKER_COUNT, Titanite.DEFAULT_MAX_REQUEST_SIZE);
        }

        public Default(int port, int ioWorkerCount, long maxRequestSize) {
            this.port = port;
            this.ioWorkerCount = ioWorkerCount;
            this.maxRequestSize = maxRequestSize;
        }

        public Default port(int port) {
            this.port = port;
            return this;
        }

        public Default ioWorkerCount(int ioWorkerCount) {
            this.ioWorkerCount = ioWorkerCount;
            return this;
        }

        public Default maxRequestSize(long maxRequestSize) {
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

}
