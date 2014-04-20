/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nosceon.titanite;

import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.HttpServerException.wrap;

/**
 * @author Johan Siebens
 */
public interface SingleParams {

    public static final Function<String, Short> SHORT = wrap(Short::valueOf);

    public static final Function<String, Integer> INT = wrap(Integer::valueOf);

    public static final Function<String, Long> LONG = wrap(Long::valueOf);

    public static final Function<String, Float> FLOAT = wrap(Float::valueOf);

    public static final Function<String, Double> DOUBLE = wrap(Double::valueOf);

    public static final Function<String, Boolean> BOOLEAN = s -> s.equals("1") || s.equals("t") || s.equals("true") || s.equals("on");

    public abstract String getString(String name);

    default String getString(String name, String defaultValue) {
        return ofNullable(getString(name)).orElse(defaultValue);
    }

    default <V> V getValue(String name, Function<String, V> f) {
        return ofNullable(getString(name)).map(f).orElse(null);
    }

    default <V> V getValue(String name, Function<String, V> f, V defaultValue) {
        return ofNullable(getString(name)).map(f).orElse(defaultValue);
    }

    default Short getShort(String name) {
        return getValue(name, SHORT);
    }

    default short getShort(String name, short defaultValue) {
        return ofNullable(getValue(name, SHORT)).orElse(defaultValue);
    }

    default Integer getInt(String name) {
        return getValue(name, INT);
    }

    default int getInt(String name, int defaultValue) {
        return ofNullable(getValue(name, INT)).orElse(defaultValue);
    }

    default Long getLong(String name) {
        return getValue(name, LONG);
    }

    default long getLong(String name, long defaultValue) {
        return ofNullable(getValue(name, LONG)).orElse(defaultValue);
    }

    default Float getFloat(String name) {
        return getValue(name, FLOAT);
    }

    default float getFloat(String name, float defaultValue) {
        return ofNullable(getValue(name, FLOAT)).orElse(defaultValue);
    }

    default Double getDouble(String name) {
        return getValue(name, DOUBLE);
    }

    default double getDouble(String name, double defaultValue) {
        return ofNullable(getValue(name, DOUBLE)).orElse(defaultValue);
    }

    default Boolean getBoolean(String name) {
        return getValue(name, BOOLEAN);
    }

    default boolean getBoolean(String name, boolean defaultValue) {
        return ofNullable(getValue(name, BOOLEAN)).orElse(defaultValue);
    }

}
