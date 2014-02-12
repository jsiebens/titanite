package org.nosceon.titanite;

import java.util.Optional;

/**
 * @author Johan Siebens
 */
public abstract class Params {

    public abstract Optional<String> getString(String name);

    public final Optional<Short> getShort(String name) {
        return getString(name).map(Short::valueOf);
    }

    public final Optional<Integer> getInt(String name) {
        return getString(name).map(Integer::valueOf);
    }

    public final Optional<Long> getLong(String name) {
        return getString(name).map(Long::valueOf);
    }

    public final Optional<Float> getFloat(String name) {
        return getString(name).map(Float::valueOf);
    }

    public final Optional<Double> getDouble(String name) {
        return getString(name).map(Double::valueOf);
    }

    public final Optional<Boolean> getBoolean(String name) {
        return getString(name).map(Boolean::valueOf);
    }

}
