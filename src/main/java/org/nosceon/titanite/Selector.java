package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpMethod;

import java.util.function.Function;

/**
 * @author Johan Siebens
 */
@FunctionalInterface
interface Selector {

    Function<Request, Response> get(HttpMethod method, String path);

}
