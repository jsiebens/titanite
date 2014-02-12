package org.nosceon.titanite;

import java.util.Map;
import java.util.Optional;

/**
 * @author Johan Siebens
 */
public final class PathParams extends Params {

    private Map<String, String> values;

    PathParams(Map<String, String> values) {
        this.values = values;
    }

    @Override
    public Optional<String> getString(String name) {
        return Optional.ofNullable(values.get(name));
    }

}
