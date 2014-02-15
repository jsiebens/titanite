package org.nosceon.titanite;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public final class HttpServerException extends RuntimeException {

    private Response response;

    public HttpServerException(Throwable cause, Response response) {
        super(cause);
        this.response = response;
    }

    public static <T> T propagate(Callable<T> callable) throws HttpServerException {
        return propagate(callable, e -> Responses.internalServerError());
    }

    public static <T> T propagate(Callable<T> callable, Function<Exception, Response> translator) throws HttpServerException {
        try {
            return callable.call();
        }
        catch (HttpServerException e) {
            throw e;
        }
        catch (Exception e) {
            throw new HttpServerException(e, translator.apply(e));
        }
    }

    public Response getResponse() {
        return response;
    }

}
