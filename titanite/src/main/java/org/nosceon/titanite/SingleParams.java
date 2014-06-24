/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

/**
 * @author Johan Siebens
 */
abstract class SingleParams {

    protected static final Function<String, Short> SHORT = Short::valueOf;

    protected static final Function<String, Integer> INT = Integer::valueOf;

    protected static final Function<String, Long> LONG = Long::valueOf;

    protected static final Function<String, Float> FLOAT = Float::valueOf;

    protected static final Function<String, Double> DOUBLE = Double::valueOf;

    protected static final Function<String, Boolean> BOOLEAN = s -> s.equals("1") || s.equals("t") || s.equals("true") || s.equals("on");

    public abstract String getString(String name);

    protected abstract IllegalArgumentException translate(Exception e, String type, String name, String value);

    private <V> V getAndTranslateValue(String name, String type, Function<String, V> f, V defaultValue) {
        return ofNullable(getString(name)).map(value -> {
            try {
                return f.apply(value);
            }
            catch (Exception e) {
                throw translate(e, type, name, value);
            }
        }).orElse(defaultValue);
    }

    public final String getString(String name, String defaultValue) {
        return ofNullable(getString(name)).orElse(defaultValue);
    }

    public final Short getShort(String name) {
        return getAndTranslateValue(name, "short", SHORT, null);
    }

    public final short getShort(String name, short defaultValue) {
        return getAndTranslateValue(name, "short", SHORT, defaultValue);
    }

    public final Integer getInt(String name) {
        return getAndTranslateValue(name, "int", INT, null);
    }

    public final int getInt(String name, int defaultValue) {
        return getAndTranslateValue(name, "int", INT, defaultValue);
    }

    public final Long getLong(String name) {
        return getAndTranslateValue(name, "long", LONG, null);
    }

    public final long getLong(String name, long defaultValue) {
        return getAndTranslateValue(name, "long", LONG, defaultValue);
    }

    public final Float getFloat(String name) {
        return getAndTranslateValue(name, "float", FLOAT, null);
    }

    public final float getFloat(String name, float defaultValue) {
        return getAndTranslateValue(name, "float", FLOAT, defaultValue);
    }

    public final Double getDouble(String name) {
        return getAndTranslateValue(name, "double", DOUBLE, null);
    }

    public final double getDouble(String name, double defaultValue) {
        return getAndTranslateValue(name, "double", DOUBLE, defaultValue);
    }

    public final Boolean getBoolean(String name) {
        return getAndTranslateValue(name, "boolean", BOOLEAN, null);
    }

    public final boolean getBoolean(String name, boolean defaultValue) {
        return getAndTranslateValue(name, "boolean", BOOLEAN, defaultValue);
    }

}
