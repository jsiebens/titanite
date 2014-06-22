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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Johan Siebens
 */
public final class Method {

    public static final Method GET = new Method("GET");

    public static final Method POST = new Method("POST");

    public static final Method PUT = new Method("PUT");

    public static final Method PATCH = new Method("PATCH");

    public static final Method DELETE = new Method("DELETE");

    private static final Map<String, Method> METHODS = new HashMap<>();

    static {
        METHODS.put(GET.toString(), GET);
        METHODS.put(POST.toString(), POST);
        METHODS.put(PUT.toString(), PUT);
        METHODS.put(PATCH.toString(), PATCH);
        METHODS.put(DELETE.toString(), DELETE);
    }

    private final String name;

    private Method(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Method)) {
            return false;
        }

        Method that = (Method) o;
        return name().equals(that.name());
    }

    @Override
    public String toString() {
        return name;
    }

    public static Method valueOf(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }

        Method result = METHODS.get(name);
        if (result != null) {
            return result;
        }
        else {
            return new Method(name);
        }
    }

}
