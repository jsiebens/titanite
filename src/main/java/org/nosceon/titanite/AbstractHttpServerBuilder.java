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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.nosceon.titanite.json.JacksonJsonMapper;
import org.nosceon.titanite.json.JsonMapper;
import org.nosceon.titanite.view.MustacheViewRenderer;
import org.nosceon.titanite.view.ViewRenderer;

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

    private Function<Request, CompletableFuture<Response>> fallback = (r) -> Responses.notFound().toFuture();

    private final List<Route<Request, CompletableFuture<Response>>> routings = new LinkedList<>();

    private Optional<Filter<Request, CompletableFuture<Response>, Request, CompletableFuture<Response>>> filter = Optional.empty();

    private Optional<JsonMapper> mapper = Optional.empty();

    private Optional<ViewRenderer> viewRenderer = Optional.empty();

    protected final String id;

    protected AbstractHttpServerBuilder(String id) {
        this.id = id;
    }

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

    public final R setMapper(JsonMapper mapper) {
        this.mapper = Optional.of(mapper);
        return self();
    }

    public final R setViewRenderer(ViewRenderer viewRenderer) {
        this.viewRenderer = Optional.of(viewRenderer);
        return self();
    }

    public final R register(Method method, String pattern, Function<Request, CompletableFuture<Response>> handler) {
        this.routings.add(new Route<>(method, pattern, handler));
        return self();
    }

    public final R register(Routes<Request, CompletableFuture<Response>> routes) {
        this.routings.addAll(routes.get());
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

    protected final void start(NioEventLoopGroup workers, int port, long maxRequestSize) {
        Router router = new Router(id, filter, routings, fallback);
        ViewRenderer viewRenderer = this.viewRenderer.orElseGet(MustacheViewRenderer::new);
        JsonMapper mapper = this.mapper.orElseGet(JacksonJsonMapper::new);

        new ServerBootstrap()
            .group(workers)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new HttpRequestDecoder())
                        .addLast(new HttpResponseEncoder())
                        .addLast(new HttpServerHandler(maxRequestSize, router, viewRenderer, mapper));
                }

            })
            .bind(port)
            .syncUninterruptibly();
    }

    protected abstract R self();

}
