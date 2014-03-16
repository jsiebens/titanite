package org.nosceon.titanite;

import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.HttpServerException.wrap;

/**
 * @author Johan Siebens
 */
public abstract class Params {

    public static final Function<String, Short> SHORT = wrap(Short::valueOf);

    public static final Function<String, Integer> INT = wrap(Integer::valueOf);

    public static final Function<String, Long> LONG = wrap(Long::valueOf);

    public static final Function<String, Float> FLOAT = wrap(Float::valueOf);

    public static final Function<String, Double> DOUBLE = wrap(Double::valueOf);

    public static final Function<String, Boolean> BOOLEAN = s -> s.equals("1") || s.equals("t") || s.equals("true") || s.equals("on");

    public abstract String get(String name);

    public final String get(String name, String defaultValue) {
        return ofNullable(get(name)).orElse(defaultValue);
    }

    public final <V> V get(String name, Function<String, V> f) {
        return ofNullable(get(name)).map(f).orElse(null);
    }

    public final <V> V get(String name, Function<String, V> f, V defaultValue) {
        return ofNullable(get(name)).map(f).orElse(defaultValue);
    }

    public final Short getShort(String name) {
        return get(name, SHORT);
    }

    public final short getShort(String name, short defaultValue) {
        return ofNullable(get(name, SHORT)).orElse(defaultValue);
    }

    public final Integer getInt(String name) {
        return get(name, INT);
    }

    public final int getInt(String name, int defaultValue) {
        return ofNullable(get(name, INT)).orElse(defaultValue);
    }

    public final Long getLong(String name) {
        return get(name, LONG);
    }

    public final long getLong(String name, long defaultValue) {
        return ofNullable(get(name, LONG)).orElse(defaultValue);
    }

    public final Float getFloat(String name) {
        return get(name, FLOAT);
    }

    public final float getInt(String name, float defaultValue) {
        return ofNullable(get(name, FLOAT)).orElse(defaultValue);
    }

    public final Double getDouble(String name) {
        return get(name, DOUBLE);
    }

    public final double getDouble(String name, double defaultValue) {
        return ofNullable(get(name, DOUBLE)).orElse(defaultValue);
    }

    public final Boolean getBoolean(String name) {
        return get(name, BOOLEAN);
    }

    public final boolean getBoolean(String name, boolean defaultValue) {
        return ofNullable(get(name, BOOLEAN)).orElse(defaultValue);
    }

}
