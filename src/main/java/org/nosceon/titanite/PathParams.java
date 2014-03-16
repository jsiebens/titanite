package org.nosceon.titanite;

import java.util.Map;

/**
 * @author Johan Siebens
 */
public final class PathParams extends Params {

    private Map<String, String> values;

    PathParams(Map<String, String> values) {
        this.values = values;
    }

    @Override
    public String get(String name) {
        return values.get(name);
    }

}
