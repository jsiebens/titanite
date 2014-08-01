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

import org.nosceon.titanite.exception.InvalidPathParamException;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * @author Johan Siebens
 */
public final class PathParams extends SingleParams {

    private Map<String, String> values;

    PathParams(Map<String, String> values) {
        this.values = values;
    }

    @Override
    public String getString(String name) {
        return values.get(name);
    }

    @Override
    public Set<String> keys() {
        return unmodifiableSet(values.keySet());
    }

    @Override
    protected IllegalArgumentException translate(Exception e, String type, String name, String value) {
        return new InvalidPathParamException(e, type, name, value);
    }

}
