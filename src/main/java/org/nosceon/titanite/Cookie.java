package org.nosceon.titanite;

import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.ServerCookieEncoder;

/**
 * @author Johan Siebens
 */
public final class Cookie {

    private final io.netty.handler.codec.http.Cookie c;

    public static Cookie newCookie(String name, String value) {
        return new Cookie(name, value);
    }

    public Cookie(String name, String value) {
        this.c = new DefaultCookie(name, value);
    }

    public Cookie domain(String value) {
        c.setDomain(value);
        return this;
    }

    public Cookie path(String value) {
        c.setPath(value);
        return this;
    }

    public Cookie comment(String value) {
        c.setComment(value);
        return this;
    }

    public Cookie commentUrl(String value) {
        c.setCommentUrl(value);
        return this;
    }

    public Cookie maxAge(long value) {
        c.setMaxAge(value);
        return this;
    }

    public Cookie discard(boolean value) {
        c.setDiscard(value);
        return this;
    }

    public Cookie httpOnly(boolean value) {
        c.setHttpOnly(value);
        return this;
    }

    public Cookie secure(boolean value) {
        c.setSecure(value);
        return this;
    }

    public Cookie version(int value) {
        c.setVersion(value);
        return this;
    }

    String encode() {
        return ServerCookieEncoder.encode(c);
    }

}
