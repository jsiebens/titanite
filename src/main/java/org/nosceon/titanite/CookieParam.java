package org.nosceon.titanite;

/**
 * @author Johan Siebens
 */
public final class CookieParam {

    private final io.netty.handler.codec.http.Cookie c;

    CookieParam(io.netty.handler.codec.http.Cookie c) {
        this.c = c;
    }

    public String name() {
        return c.getName();
    }

    public String value() {
        return c.getValue();
    }

}
