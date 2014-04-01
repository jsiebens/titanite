package org.nosceon.titanite;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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

    private Optional<Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>>> filter = Optional.empty();

    private Optional<ObjectMapper> mapper = Optional.empty();

    @SafeVarargs
    public final R setFilter(Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>> filter, Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>>... additionalFilters) {
        Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>> f = filter;
        if (additionalFilters != null) {
            for (Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>> additionalFilter : additionalFilters) {
                f = f.andThen(additionalFilter);
            }
        }
        this.filter = Optional.of(f);
        return self();
    }

    public final R setMapper(ObjectMapper mapper) {
        this.mapper = Optional.of(mapper);
        return self();
    }

    public final R register(Method method, String pattern, Function<Request, CompletableFuture<Response>> handler) {
        this.routings.add(new Routing<>(method, pattern, handler));
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

    public final R notFound(Function<Request, CompletableFuture<Response>> handler) {
        this.fallback = handler;
        return self();
    }

    protected final Router router(String id) {
        return new Router(id, filter, routings, fallback);
    }

    protected final ObjectMapper mapper() {
        return mapper.orElseGet(ObjectMapper::new);
    }

    protected abstract R self();

}
