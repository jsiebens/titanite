package org.nosceon.titanite;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
final class RoutingResult {

    public final Map<String, String> pathParams;

    public final Function<Request, CompletableFuture<Response>> function;

    public RoutingResult(Map<String, String> pathParams, Function<Request, CompletableFuture<Response>> function) {
        this.pathParams = pathParams;
        this.function = function;
    }

}
