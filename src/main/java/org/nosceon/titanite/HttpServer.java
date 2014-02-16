package org.nosceon.titanite;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static org.nosceon.titanite.Chain.newChain;
import static org.nosceon.titanite.service.ResourceService.PUBLIC_RESOURCES;
import static org.nosceon.titanite.service.ResourceService.WEBJAR_RESOURCES;

/**
 * @author Johan Siebens
 */
public final class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;

    private int executorThreadCount = 16;

    private int maxRequestSize = 1024 * 1024 * 10;

    private Function<Request, Response> fallback = newChain(PUBLIC_RESOURCES).fallbackTo(WEBJAR_RESOURCES);

    private final List<Routing<Request, Response>> routings = new LinkedList<>();

    private final List<Filter<Request, Response, Request, Response>> filters = new LinkedList<>();

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

    public HttpServer get(String pattern, Function<Request, Response> function) {
        return add(HttpMethod.GET, pattern, function);
    }

    public HttpServer post(String pattern, Function<Request, Response> function) {
        return add(HttpMethod.POST, pattern, function);
    }

    public HttpServer put(String pattern, Function<Request, Response> function) {
        return add(HttpMethod.PUT, pattern, function);
    }

    public HttpServer patch(String pattern, Function<Request, Response> function) {
        return add(HttpMethod.PATCH, pattern, function);
    }

    public HttpServer delete(String pattern, Function<Request, Response> function) {
        return add(HttpMethod.DELETE, pattern, function);
    }

    public HttpServer register(Filter<Request, Response, Request, Response> filter) {
        this.filters.add(filter);
        return this;
    }

    public HttpServer register(Routings<Request, Response> routings) {
        this.routings.addAll(routings.get());
        return this;
    }

    public HttpServer notFound(Function<Request, Response> fallback) {
        this.fallback = fallback;
        return this;
    }

    private HttpServer add(HttpMethod method, String pattern, Function<Request, Response> function) {
        this.routings.add(new Routing<>(method, pattern, function));
        return this;
    }

    public Shutdownable start(int port) {
        logger.info("Http Server starting");

        Router router = new Router(filters, routings, fallback);
        ViewRenderer renderer = new ViewRenderer();
        ObjectMapper mapper = new ObjectMapper();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(ioWorkerCount);
        EventLoopGroup eventExecutor = new NioEventLoopGroup(executorThreadCount);

        new ServerBootstrap()
            .group(eventLoopGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new HttpRequestDecoder())
                        .addLast(new HttpResponseEncoder())
                        .addLast(eventExecutor, new HttpServerHandler(maxRequestSize, router, renderer, mapper));
                }

            })
            .bind(port).syncUninterruptibly();

        logger.info("Http Server started, listening on port " + port);

        return () -> {
            eventExecutor.shutdownGracefully();
            eventLoopGroup.shutdownGracefully();
        };
    }

}
