package org.nosceon.titanite;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.nosceon.titanite.Responses.notFound;

/**
 * @author Johan Siebens
 */
public final class Chain implements Function<Request, CompletableFuture<Response>> {

    private Function<Request, CompletableFuture<Response>> first;

    private Function<Request, CompletableFuture<Response>> second;

    public static Chain newChain(Function<Request, CompletableFuture<Response>> function) {
        return new Chain(function);
    }

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
                    if (resp.status() == 404) {
                        return second.apply(request);
                    }
                    return CompletableFuture.completedFuture(resp);
                });
    }

    public Chain fallbackTo(Function<Request, CompletableFuture<Response>> next) {
        return new Chain(this, next);
    }

}
