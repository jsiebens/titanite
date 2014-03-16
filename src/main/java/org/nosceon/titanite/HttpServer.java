package org.nosceon.titanite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Johan Siebens
 */
public final class HttpServer extends AbstractHttpServerBuilder<HttpServer> {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;

    private int maxRequestSize = 1024 * 1024 * 10;

    public HttpServer() {
    }

    public HttpServer(int ioWorkerCount) {
        this.ioWorkerCount = ioWorkerCount;
    }

    public HttpServer(int ioWorkerCount, int maxRequestSize) {
        this.ioWorkerCount = ioWorkerCount;
        this.maxRequestSize = maxRequestSize;
    }

    public Shutdownable start(int port) {
        String id = Strings.padStart(String.valueOf(COUNTER.incrementAndGet()), 3, '0');

        logger.info("Http Server [" + id + "] starting");

        Router router = router(id);
        ViewRenderer renderer = new ViewRenderer();
        ObjectMapper mapper = mapper();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(ioWorkerCount);

        newHttpServerBootstrap(eventLoopGroup, maxRequestSize, router, renderer, mapper).bind(port).syncUninterruptibly();

        logger.info("Http Server [" + id + "] started, listening on port " + port);

        return eventLoopGroup::shutdownGracefully;
    }

    @Override
    protected HttpServer self() {
        return this;
    }

}
