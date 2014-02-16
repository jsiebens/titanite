package org.nosceon.titanite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Johan Siebens
 */
public final class HttpServer extends RouterBuilder<HttpServer> {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    public static ServerBootstrap newHttpServerBootstrap(EventLoopGroup ioWorkers, EventLoopGroup executors, long maxRequestSize, Router router, ViewRenderer renderer, ObjectMapper mapper) {
        return new ServerBootstrap()
            .group(ioWorkers)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new HttpRequestDecoder())
                        .addLast(new HttpResponseEncoder())
                        .addLast(executors, new HttpServerHandler(maxRequestSize, router, renderer, mapper));
                }

            });
    }

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;

    private int executorThreadCount = 16;

    private int maxRequestSize = 1024 * 1024 * 10;

    public HttpServer() {
    }

    public HttpServer(int ioWorkerCount, int executorThreadCount) {
        this.ioWorkerCount = ioWorkerCount;
        this.executorThreadCount = executorThreadCount;
    }

    public HttpServer(int ioWorkerCount, int executorThreadCount, int maxRequestSize) {
        this.ioWorkerCount = ioWorkerCount;
        this.executorThreadCount = executorThreadCount;
        this.maxRequestSize = maxRequestSize;
    }

    public Shutdownable start(int port) {
        String id = Strings.padStart(String.valueOf(COUNTER.incrementAndGet()), 3, '0');

        logger.info("Http Server [" + id + "] starting");

        Router router = router(id);
        ViewRenderer renderer = new ViewRenderer();
        ObjectMapper mapper = new ObjectMapper();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(ioWorkerCount);
        EventLoopGroup eventExecutor = new NioEventLoopGroup(executorThreadCount);

        newHttpServerBootstrap(eventLoopGroup, eventExecutor, maxRequestSize, router, renderer, mapper).bind(port).syncUninterruptibly();

        logger.info("Http Server [" + id + "] started, listening on port " + port);

        return () -> {
            eventExecutor.shutdownGracefully();
            eventLoopGroup.shutdownGracefully();
        };
    }

    @Override
    protected HttpServer self() {
        return this;
    }

}
