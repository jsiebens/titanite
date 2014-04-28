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
public interface SingleParams extends Params {

    public abstract String getString(String name);

    default String getString(String name, String defaultValue) {
        return ofNullable(getString(name)).orElse(defaultValue);
    }

    default <V> V getValue(String name, Function<String, V> f) {
        return ofNullable(getString(name)).map(wrap(f)).orElse(null);
    }

    default <V> V getValue(String name, Function<String, V> f, V defaultValue) {
        return ofNullable(getString(name)).map(wrap(f)).orElse(defaultValue);
    }

    default Short getShort(String name) {
        return getValue(name, SHORT);
    }

    default short getShort(String name, short defaultValue) {
        return getValue(name, SHORT, defaultValue);
    }

    default Integer getInt(String name) {
        return getValue(name, INT);
    }

    default int getInt(String name, int defaultValue) {
        return getValue(name, INT, defaultValue);
    }

    default Long getLong(String name) {
        return getValue(name, LONG);
    }

    default long getLong(String name, long defaultValue) {
        return getValue(name, LONG, defaultValue);
    }

    default Float getFloat(String name) {
        return getValue(name, FLOAT);
    }

    default float getFloat(String name, float defaultValue) {
        return getValue(name, FLOAT, defaultValue);
    }

    default Double getDouble(String name) {
        return getValue(name, DOUBLE);
    }

    default double getDouble(String name, double defaultValue) {
        return getValue(name, DOUBLE, defaultValue);
    }

    default Boolean getBoolean(String name) {
        return getValue(name, BOOLEAN);
    }

    default boolean getBoolean(String name, boolean defaultValue) {
        return getValue(name, BOOLEAN, defaultValue);
    }

}
