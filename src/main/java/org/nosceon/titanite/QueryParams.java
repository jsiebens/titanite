package org.nosceon.titanite;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Johan Siebens
 */
public final class QueryParams extends Params {

    private Map<String, List<String>> values;

    QueryParams(Map<String, List<String>> values) {
        this.values = values;
    }

    @Override
    public Optional<String> getString(String name) {
        return
            Optional.ofNullable(values.get(name))
                .filter(l -> !l.isEmpty())
                .map((l) -> l.get(0));
    }

}
