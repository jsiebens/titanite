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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.nosceon.titanite.Exceptions.internalServerError;

/**
 * @author Johan Siebens
 */
@SuppressWarnings("unchecked")
public final class ExceptionsFilter implements Filter {

    private final Map<Class<? extends Throwable>, BiFunction<Request, Throwable, Response>> handlers = new LinkedHashMap<>();

    public static ExceptionsFilter onException() {
        return new ExceptionsFilter();
    }

    public <T extends Throwable> ExceptionsFilter match(Class<T> type, BiFunction<Request, T, Response> handler) {
        this.handlers.put(type, (BiFunction<Request, Throwable, Response>) handler);
        return this;
    }

    public <T extends Throwable> ExceptionsFilter match(Class<T> type, Supplier<Response> handler) {
        return match(type, (req, t) -> handler.get());
    }

    @Override
    public CompletionStage<Response> apply(Request request, Function<Request, CompletionStage<Response>> function) {
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
            e = lookupCause(e);
        }

        if (e instanceof InternalRuntimeException) {
            e = lookupCause(e);
        }

        BiFunction<Request, Throwable, Response> function = find(e.getClass());

        if (function != null) {
            return function.apply(request, e);
        }

        if (e instanceof HttpServerException) {
            Response response = ((HttpServerException) e).getResponse();

            if (response.status() >= 500) {
                Titanite.LOG.error("error processing request", e);
            }

            return response;
        }
        else {
            Titanite.LOG.error("error processing request", e);
            return internalServerError();
        }

    }

    private Throwable lookupCause(Throwable e) {
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
