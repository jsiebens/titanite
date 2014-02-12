package org.nosceon.titanite;

import java.util.Map;

/**
 * @author Johan Siebens
 */
final class RoutingResult {

    public final Map<String, String> pathParams;

    public final Selector selector;

    public RoutingResult(Map<String, String> pathParams, Selector selector) {
        this.pathParams = pathParams;
        this.selector = selector;
    }

}
