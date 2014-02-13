package org.nosceon.titanite;

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
import java.util.function.Supplier;

/**
 * @author Johan Siebens
 */
public final class HttpServer {

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

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final List<Routing> routings = new LinkedList<>();

    public HttpServer get(String pattern, Supplier<Response> function) {
        return add(HttpMethod.GET, pattern, (r) -> function.get());
    }

    public HttpServer get(String pattern, Function<Request, Response> function) {
        return add(HttpMethod.GET, pattern, function);
    }

    public HttpServer post(String pattern, Supplier<Response> function) {
        return add(HttpMethod.POST, pattern, (r) -> function.get());
    }

    public HttpServer post(String pattern, Function<Request, Response> function) {
        return add(HttpMethod.POST, pattern, function);
    }

    public HttpServer put(String pattern, Supplier<Response> function) {
        return add(HttpMethod.PUT, pattern, (r) -> function.get());
    }

    public HttpServer put(String pattern, Function<Request, Response> function) {
        return add(HttpMethod.PUT, pattern, function);
    }

    public HttpServer patch(String pattern, Supplier<Response> function) {
        return add(HttpMethod.PATCH, pattern, (r) -> function.get());
    }

    public HttpServer patch(String pattern, Function<Request, Response> function) {
        return add(HttpMethod.PATCH, pattern, function);
    }

    public HttpServer delete(String pattern, Supplier<Response> function) {
        return add(HttpMethod.DELETE, pattern, (r) -> function.get());
    }

    public HttpServer delete(String pattern, Function<Request, Response> function) {
        return add(HttpMethod.DELETE, pattern, function);
    }

    private HttpServer add(HttpMethod method, String pattern, Function<Request, Response> function) {
        routings.add(new Routing(method, pattern, function));
        return this;
    }

    public Stopable start(int port) {
        logger.info("Http Server starting");

        Router router = new Router(routings);
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
                        .addLast(eventExecutor, new HttpServerHandler(maxRequestSize, router));
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
