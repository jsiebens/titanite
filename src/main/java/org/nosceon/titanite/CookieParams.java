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

    public Optional<CookieParam> get(String name) {
        return Optional.ofNullable(cookies.get(name));
    }

    @Override
    public Optional<String> getString(String name) {
        return get(name).map(CookieParam::value);
    }

}
