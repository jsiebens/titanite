package org.nosceon.titanite;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.nosceon.titanite.HttpServerException.propagate;

/**
 * @author Johan Siebens
 */
public abstract class AbstractHttpServerBuilder<R extends AbstractHttpServerBuilder> {

    public static ServerBootstrap newHttpServerBootstrap(EventLoopGroup ioWorkers, long maxRequestSize, Router router, ViewRenderer renderer, ObjectMapper mapper) {
        return new ServerBootstrap()
            .group(ioWorkers)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new HttpRequestDecoder())
                        .addLast(new HttpResponseEncoder())
                        .addLast(new HttpServerHandler(maxRequestSize, router, renderer, mapper));
                }

            });
    }

    private Function<Request, CompletableFuture<Response>> fallback = (r) -> Responses.notFound().toFuture();

    private final List<Routing<Request, CompletableFuture<Response>>> routings = new LinkedList<>();

    private final List<Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>>> filters = new LinkedList<>();

    private final Map<Class<? extends Throwable>, BiFunction<Request, Throwable, Response>> errorHandlers = new LinkedHashMap<>();

    private Optional<ObjectMapper> mapper = Optional.empty();

    public final R register(Method method, String pattern, Function<Request, CompletableFuture<Response>> function) {
        this.routings.add(new Routing<>(method, pattern, function));
        return self();
    }

    public final R register(Routings<Request, CompletableFuture<Response>> routings) {
        this.routings.addAll(routings.get());
        return self();
    }

    public final R register(Class<? extends Controller> c) {
        Controller controller = propagate(c::newInstance);
        return register(controller);
    }

    public final R register(Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>> filter) {
        this.filters.add(filter);
        return self();
    }

    public final R register(ObjectMapper mapper) {
        this.mapper = Optional.of(mapper);
        return self();
    }

    public final R notFound(Function<Request, CompletableFuture<Response>> fallback) {
        this.fallback = fallback;
        return self();
    }

    public final <T extends Throwable> R error(Class<T> type, BiFunction<Request, T, Response> function) {
        this.errorHandlers.putIfAbsent(type, (BiFunction<Request, Throwable, Response>) function);
        return self();
    }

    public final <T extends Throwable> R error(Class<T> type, Supplier<Response> supplier) {
        this.errorHandlers.putIfAbsent(type, (r, e) -> supplier.get());
        return self();
    }

    protected final Router router(String id) {
        return new Router(id, errorHandlers, filters, routings, fallback);
    }

    protected final ObjectMapper mapper() {
        return mapper.orElseGet(ObjectMapper::new);
    }

    protected abstract R self();

}
