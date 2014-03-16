package org.nosceon.titanite;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author Johan Siebens
 */
public final class CookieParams extends Params {

    private final Map<String, CookieParam> cookies;

    CookieParams() {
        this(Collections.emptyMap());
    }

    CookieParams(Map<String, CookieParam> cookies) {
        this.cookies = cookies;
    }

    public CookieParam getCookie(String name) {
        return cookies.get(name);
    }

    @Override
    public String get(String name) {
        return Optional.ofNullable(cookies.get(name)).map(CookieParam::value).orElse(null);
    }

}
