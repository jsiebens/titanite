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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.nosceon.titanite.HttpServerException.call;

/**
 * @author Johan Siebens
 */
public abstract class AbstractHttpServerBuilder<R extends AbstractHttpServerBuilder> {

    private final List<Route> routings = new LinkedList<>();

    private BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> globalFilter;

    protected final String id;

    protected AbstractHttpServerBuilder(String id) {
        this.id = id;
    }

    public final R setFilter(BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter) {
        this.globalFilter = filter;
        return self();
    }

    public final R register(Method method, String pattern, Function<Request, CompletionStage<Response>> handler) {
        this.routings.add(new Route(method, pattern, handler));
        return self();
    }

    public final R register(Method method, String pattern, BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter, Function<Request, CompletionStage<Response>> handler) {
        return register(method, pattern, new CompositeHandler(filter, handler));
    }

    public final R register(Controller controller) {
        this.routings.addAll(controller.routes());
        return self();
    }

    public final R register(BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter, Controller controller) {
        this.routings.addAll(controller.routes().stream().map(r -> new Route(r.method(), r.pattern(), new CompositeHandler(filter, r.function()))).collect(toList()));
        return self();
    }

    public final R register(Class<? extends Controller> c) {
        return register(call(c::newInstance));
    }

    public final R register(BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter, Class<? extends Controller> c) {
        return register(filter, call(c::newInstance));
    }

    protected final void start(NioEventLoopGroup workers, int port, long maxRequestSize) {
        List<Route> actualRoutes = applyGlobalFilter();
        actualRoutes.forEach(r -> Titanite.LOG.info(id + " route added: " + Utils.padEnd(r.method().toString(), 7, ' ') + r.pattern()));

        Router router = new Router(actualRoutes);

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

    private List<Route> applyGlobalFilter() {
        if (globalFilter != null) {
            return routings.stream().map(r -> {
                Function<Request, CompletionStage<Response>> handler = new CompositeHandler(globalFilter, r.function());
                return new Route(r.method(), r.pattern(), handler);
            }).collect(toList());
        }
        else {
            return routings;
        }
    }

    protected abstract R self();

}
