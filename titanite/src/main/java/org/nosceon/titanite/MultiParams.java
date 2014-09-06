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

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * @author Johan Siebens
 */
public abstract class MultiParams extends SingleParams {

    public abstract List<String> getStrings(String name);

    private <V> List<V> getAndTranslateValues(String name, String type, Function<String, V> f) {
        return getStrings(name).stream().map(value -> {
            try {
                return f.apply(value);
            }
            catch (Exception e) {
                throw translate(e, type, name, value);
            }
        }).collect(toList());
    }

    public final List<Short> getShorts(String name) {
        return getAndTranslateValues(name, "short", SHORT);
    }

    public final List<Integer> getInts(String name) {
        return getAndTranslateValues(name, "int", INT);
    }

    public final List<Long> getLongs(String name) {
        return getAndTranslateValues(name, "long", LONG);
    }

    public final List<Float> getFloats(String name) {
        return getAndTranslateValues(name, "float", FLOAT);
    }

    public final List<Double> getDoubles(String name) {
        return getAndTranslateValues(name, "double", DOUBLE);
    }

    public final List<Boolean> getBooleans(String name) {
        return getAndTranslateValues(name, "boolean", BOOLEAN);
    }

}
