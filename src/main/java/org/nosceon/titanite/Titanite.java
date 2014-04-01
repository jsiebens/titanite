package org.nosceon.titanite;

import org.nosceon.titanite.service.FileService;
import org.nosceon.titanite.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public final class Titanite extends Responses {

    static final Logger LOG = LoggerFactory.getLogger(Titanite.class);

    public static final Function<Request, Response> WEBJAR_RESOURCES = resourceService("/META-INF/resources/webjars");

    public static final Function<Request, Response> PUBLIC_RESOURCES = resourceService("/public");

    public static HttpServer httpServer() {
        return new HttpServer();
    }

    public static HttpServer httpServer(int ioWorkerCount) {
        return new HttpServer(ioWorkerCount);
    }

    public static HttpServer httpServer(int ioWorkerCount, int maxRequestSize) {
        return new HttpServer(ioWorkerCount, maxRequestSize);
    }

    public static ErrorFilter errors() {
        return new ErrorFilter();
    }

    @SafeVarargs
    public static Function<Request, CompletableFuture<Response>> compose(Function<Request, CompletableFuture<Response>> function, Function<Request, CompletableFuture<Response>>... functions) {
        Chain chain = new Chain(function);
        if (functions != null) {
            for (Function<Request, CompletableFuture<Response>> f : functions) {
                chain = chain.fallbackTo(f);
            }
        }
        return chain;
    }

    public static Function<Request, Response> fileService(String directory) {
        return new FileService(new File(directory));
    }

    public static Function<Request, Response> fileService(File directory) {
        return new FileService(directory);
    }

    public static Function<Request, Response> resourceService(String baseResource) {
        return new ResourceService(baseResource);
    }

    public static Function<Request, CompletableFuture<Response>> sync(Function<Request, Response> f) {
        return (r) -> CompletableFuture.completedFuture(f.apply(r));
    }

    public static Function<Request, CompletableFuture<Response>> async(Function<Request, Response> f) {
        return (r) -> CompletableFuture.supplyAsync(() -> f.apply(r));
    }

    public static Function<Request, CompletableFuture<Response>> async(Function<Request, Response> f, Executor executor) {
        return (r) -> CompletableFuture.supplyAsync(() -> f.apply(r), executor);
    }

    private static final class Chain implements Function<Request, CompletableFuture<Response>> {

        private Function<Request, CompletableFuture<Response>> first;

        private Function<Request, CompletableFuture<Response>> second;

        private Chain(Function<Request, CompletableFuture<Response>> function) {
            this((r) -> notFound().toFuture(), function);
        }

        private Chain(Function<Request, CompletableFuture<Response>> first, Function<Request, CompletableFuture<Response>> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public CompletableFuture<Response> apply(Request request) {
            return
                first.apply(request)
                    .thenCompose(resp -> {
                        if (resp == null || resp.status() == 404) {
                            return second.apply(request);
                        }
                        return CompletableFuture.completedFuture(resp);
                    });
        }

        private Chain fallbackTo(Function<Request, CompletableFuture<Response>> next) {
            return new Chain(this, next);
        }

    }

}
