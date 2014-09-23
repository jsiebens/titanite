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
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.nosceon.titanite.body.BodyParser;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.nosceon.titanite.ImmutableSettings.newSettings;
import static org.nosceon.titanite.Utils.callUnchecked;

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
        this.routings.add(new Route(method, pattern, null, handler));
        return self();
    }

    public final R register(Method method, String pattern, Supplier<BodyParser> bodyParser, Function<Request, CompletionStage<Response>> handler) {
        this.routings.add(new Route(method, pattern, bodyParser, handler));
        return self();
    }

    public final R register(Method method, String pattern, BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter, Function<Request, CompletionStage<Response>> handler) {
        return register(method, pattern, new CompositeHandler(filter, handler));
    }

    public final R register(Method method, String pattern, Supplier<BodyParser> bodyParser, BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter, Function<Request, CompletionStage<Response>> handler) {
        return register(method, pattern, bodyParser, new CompositeHandler(filter, handler));
    }

    public final R register(Controller controller) {
        this.routings.addAll(controller.routes());
        return self();
    }

    public final R register(BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter, Controller controller) {
        this.routings.addAll(controller.routes().stream().map(r -> new Route(r.method(), r.pattern(), null, new CompositeHandler(filter, r.handler()))).collect(toList()));
        return self();
    }

    public final R register(Class<? extends Controller> c) {
        return register(callUnchecked(c::newInstance));
    }

    public final R register(BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter, Class<? extends Controller> c) {
        return register(filter, callUnchecked(c::newInstance));
    }

    protected final void start(NioEventLoopGroup workers, Settings settings) {
        List<Route> actualRoutes = applyGlobalFilter();
        actualRoutes.forEach(r -> Titanite.LOG.info(id + " route added: " + Utils.padEnd(r.method().toString(), 7, ' ') + r.pattern()));

        Router router = new Router(actualRoutes);

        settings.connectors().forEach(c -> {
            if (c.type().equals(Settings.ConnectorType.HTTP)) {
                Channel channel = bootstrap(workers, null, settings, c, router);

                Titanite.LOG.info(id + " listening for " + Utils.padEnd(c.type().name(), 6, ' ') + " on " + channel.localAddress());
            }
            else if (c.type().equals(Settings.ConnectorType.HTTPS)) {
                SslContext sslContext = sslContext(c);
                Channel channel = bootstrap(workers, sslContext, settings, c, router);

                Titanite.LOG.info(id + " listening for " + Utils.padEnd(c.type().name(), 6, ' ') + " on " + channel.localAddress());
            }
        });

    }

    private Channel bootstrap(NioEventLoopGroup workers, SslContext sslContext, Settings settings, Settings.Connector c, Router router) {
        return
            new ServerBootstrap()
                .group(workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpServerChannelInitializer(sslContext, settings, router))
                .bind(inetAddress(c))
                .syncUninterruptibly()
                .channel();
    }

    private InetSocketAddress inetAddress(Settings.Connector c) {
        return Optional.ofNullable(c.address()).map(a -> new InetSocketAddress(c.address(), c.port())).orElseGet(() -> new InetSocketAddress(c.port()));
    }

    @Deprecated
    protected final void start(NioEventLoopGroup workers, HttpServerConfig config) {
        this.start(
            workers,
            newSettings()
                .setIoWorkerCount(config.getIoWorkerCount())
                .setMaxRequestSize(config.getMaxRequestSize())
                .setMaxMultipartRequestSize(config.getMaxMultipartRequestSize())
                .addHttpConnector(config.getPort())
                .build()
        );
    }

    private List<Route> applyGlobalFilter() {
        if (globalFilter != null) {
            return routings.stream().map(r -> {
                Function<Request, CompletionStage<Response>> handler = new CompositeHandler(globalFilter, r.handler());
                return new Route(r.method(), r.pattern(), r.bodyParser(), handler);
            }).collect(toList());
        }
        else {
            return routings;
        }
    }

    private SslContext sslContext(Settings.Connector connector) {
        return callUnchecked(() -> {
            if (connector.certificatePath() == null || connector.keyPath() == null) {

                Titanite.LOG.warn(id + " ssl certificate path or key path is missing, using self-signed certificate");

                SelfSignedCertificate ssc = new SelfSignedCertificate();
                return SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            }
            else {
                return SslContext.newServerContext(connector.certificatePath(), connector.keyPath(), connector.keyPassword());
            }
        });
    }

    protected abstract R self();

    private static class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {

        private final SslContext sslCtx;

        private final Settings settings;

        private final Router router;

        public HttpServerChannelInitializer(SslContext sslCtx, Settings settings, Router router) {
            this.sslCtx = sslCtx;
            this.settings = settings;
            this.router = router;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            if (sslCtx != null) {
                pipeline.addLast(sslCtx.newHandler(ch.alloc()));
            }

            pipeline
                .addLast(new HttpRequestDecoder())
                .addLast(new HttpResponseEncoder())
                .addLast(new ChunkedWriteHandler())
                .addLast(new HttpServerHandler(sslCtx != null, settings.maxRequestSize(), settings.maxMultipartRequestSize(), router));
        }

    }
}
