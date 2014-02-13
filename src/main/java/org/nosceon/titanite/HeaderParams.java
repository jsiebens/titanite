package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.Optional;

/**
 * @author Johan Siebens
 */
public final class HeaderParams extends Params {

    private HttpHeaders headers;

    HeaderParams(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public Optional<String> getString(String name) {
        return Optional.ofNullable(headers.get(name));
    }

}
