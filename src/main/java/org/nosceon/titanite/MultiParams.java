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

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * @author Johan Siebens
 */
public interface MultiParams {

    List<String> getStrings(String name);

    default <V> List<V> getValues(String name, Function<String, V> f) {
        return getStrings(name).stream().map(f).collect(toList());
    }

    default List<Short> getShorts(String name) {
        return getValues(name, SingleParams.SHORT);
    }

    default List<Integer> getInts(String name) {
        return getValues(name, SingleParams.INT);
    }

    default List<Long> getLongs(String name) {
        return getValues(name, SingleParams.LONG);
    }

    default List<Float> getFloats(String name) {
        return getValues(name, SingleParams.FLOAT);
    }

    default List<Double> getDoubles(String name) {
        return getValues(name, SingleParams.DOUBLE);
    }

    default List<Boolean> getBooleans(String name) {
        return getValues(name, SingleParams.BOOLEAN);
    }

}
