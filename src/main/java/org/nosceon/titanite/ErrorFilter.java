package org.nosceon.titanite;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.nosceon.titanite.Responses.internalServerError;

/**
 * @author Johan Siebens
 */
final class ErrorFilter implements SimpleFilter<Request, CompletableFuture<Response>> {

    private final Map<Class<? extends Throwable>, BiFunction<Request, Throwable, Response>> handlers = new LinkedHashMap<>();

    public ErrorFilter(Map<Class<? extends Throwable>, BiFunction<Request, Throwable, Response>> handlers) {
        this.handlers.putAll(handlers);
    }

    @Override
    public CompletableFuture<Response> apply(Request request, Function<? super Request, ? extends CompletableFuture<Response>> function) {
        try {
            return function.apply(request).exceptionally(t -> translate(request, t));
        }
        catch (Exception e) {
            return translate(request, e).toFuture();
        }
    }

    private Response translate(Request request, Throwable t) {
        Throwable e = t;

        if (e instanceof CompletionException) {
            e = lookupCause((CompletionException) e);
        }

        BiFunction<Request, Throwable, Response> function = find(e.getClass());

        if (function != null) {
            return function.apply(request, t);
        }

        if (e instanceof HttpServerException) {
            return ((HttpServerException) e).getResponse();
        }

        Titanite.LOG.error("error processing request", e);

        return internalServerError().text("Internal Server Error");
    }

    private Throwable lookupCause(CompletionException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            return cause;
        }
        return e;
    }

    public BiFunction<Request, Throwable, Response> find(Class<? extends Throwable> c) {
        int distance = Integer.MAX_VALUE;
        BiFunction<Request, Throwable, Response> selectedSupplier = null;

        for (Map.Entry<Class<? extends Throwable>, BiFunction<Request, Throwable, Response>> entry : handlers.entrySet()) {
            int d = distance(c, entry.getKey());
            if (d < distance) {
                distance = d;
                selectedSupplier = entry.getValue();
                if (distance == 0) {
                    break;
                }
            }
        }

        return selectedSupplier;
    }

    private int distance(Class<?> c, Class<?> key) {
        int distance = 0;
        if (!key.isAssignableFrom(c)) {
            return Integer.MAX_VALUE;
        }

        while (c != key) {
            c = c.getSuperclass();
            distance++;
        }

        return distance;
    }

}
