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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.nosceon.titanite.HttpServerException.propagate;

/**
 * @author Johan Siebens
 */
public abstract class AbstractHttpServerBuilder<R extends AbstractHttpServerBuilder> {

    private Function<Request, CompletionStage<Response>> fallback = (r) -> Titanite.Responses.notFound().toFuture();

    private final List<Route> routings = new LinkedList<>();

    private Optional<Filter> filter = Optional.empty();

    protected final String id;

    protected AbstractHttpServerBuilder(String id) {
        this.id = id;
    }

    public final R setFilter(Filter filter, Filter... additionalFilters) {
        Filter f = filter;
        if (additionalFilters != null) {
            for (Filter additionalFilter : additionalFilters) {
                f = f.andThen(additionalFilter);
            }
        }
        this.filter = Optional.of(f);
        return self();
    }

    @SafeVarargs
    public final R register(Method method, String pattern, Function<Request, CompletionStage<Response>> handler, Function<Request, CompletionStage<Response>>... handlers) {
        if (handlers != null && handlers.length > 0) {
            Chain chain = new Chain(handler);
            for (Function<Request, CompletionStage<Response>> f : handlers) {
                chain = chain.fallbackTo(f);
            }

            this.routings.add(new Route(method, pattern, chain));
        }
        else {
            this.routings.add(new Route(method, pattern, handler));
        }
        return self();
    }

    public final R register(Controller controller) {
        this.routings.addAll(controller.get());
        return self();
    }

    public final R register(Class<? extends Controller> c) {
        Controller controller = propagate(c::newInstance);
        return register(controller);
    }

    protected final void start(NioEventLoopGroup workers, int port, long maxRequestSize) {
        Router router = new Router(id, filter, routings, fallback);

        new ServerBootstrap()
            .group(workers)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                        .addLast(new HttpRequestDecoder())
                        .addLast(new HttpResponseEncoder())
                        .addLast(new HttpServerHandler(maxRequestSize, router));
                }

            })
            .bind(port)
            .syncUninterruptibly();
    }

    protected abstract R self();

    private static final class Chain implements Function<Request, CompletionStage<Response>> {

        private Function<Request, CompletionStage<Response>> first;

        private Function<Request, CompletionStage<Response>> second;

        private Chain(Function<Request, CompletionStage<Response>> function) {
            this((r) -> Titanite.Responses.notFound().toFuture(), function);
        }

        private Chain(Function<Request, CompletionStage<Response>> first, Function<Request, CompletionStage<Response>> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public CompletionStage<Response> apply(Request request) {
            return
                first.apply(request)
                    .thenCompose(resp -> {
                        if (resp == null || resp.status() == 404) {
                            return second.apply(request);
                        }
                        return completedFuture(resp);
                    });
        }

        private Chain fallbackTo(Function<Request, CompletionStage<Response>> next) {
            return new Chain(this, next);
        }

    }

}
