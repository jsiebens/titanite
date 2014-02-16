package org.nosceon.titanite;

import java.util.function.Function;

import static org.nosceon.titanite.Responses.notFound;

/**
 * @author Johan Siebens
 */
public class Chain implements Function<Request, Response> {

    private Function<Request, Response> first;

    private Function<Request, Response> second;

    public static Chain newChain(Function<Request, Response> function) {
        return new Chain(function);
    }

    private Chain(Function<Request, Response> function) {
        this((r) -> notFound(), function);
    }

    private Chain(Function<Request, Response> first, Function<Request, Response> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public Response apply(Request request) {
        Response response = first.apply(request);
        if (response.status() == 404) {
            return second.apply(request);
        }
        return response;
    }

    public Chain fallbackTo(Function<Request, Response> next) {
        return new Chain(this, next);
    }

}
