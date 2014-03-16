package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

/**
 * @author Johan Siebens
 */
public final class HeaderParams extends Params {

    private HttpMessage message;

    HeaderParams(HttpMessage message) {
        this.message = message;
    }

    @Override
    public String get(String name) {
        return HttpHeaders.getHeader(message, name);
    }

    public Date getDate(String name) {
        return Optional.ofNullable(HttpHeaders.getHeader(message, name))
            .flatMap((o) -> {
                try {
                    return Optional.of(HttpHeaders.getDateHeader(message, name));
                }
                catch (ParseException e) {
                    return Optional.empty();
                }
            }).orElse(null);
    }

}
