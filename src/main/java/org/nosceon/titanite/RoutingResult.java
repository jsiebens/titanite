package org.nosceon.titanite;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
final class RoutingResult {

    public final Map<String, String> pathParams;

    public final Function<Request, Response> function;

    public RoutingResult(Map<String, String> pathParams, Function<Request, Response> function) {
        this.pathParams = pathParams;
        this.function = function;
    }

}
