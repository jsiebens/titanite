package org.nosceon.titanite;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public abstract class Controller extends Routings<Request, CompletableFuture<Response>> {

    public static Function<Request, CompletableFuture<Response>> sync(Function<Request, Response> f) {
        return (r) -> CompletableFuture.completedFuture(f.apply(r));
    }

    public static Function<Request, CompletableFuture<Response>> async(Function<Request, Response> f) {
        return (r) -> CompletableFuture.supplyAsync(() -> f.apply(r));
    }

    public static Function<Request, CompletableFuture<Response>> async(Function<Request, Response> f, Executor executor) {
        return (r) -> CompletableFuture.supplyAsync(() -> f.apply(r), executor);
    }

}
