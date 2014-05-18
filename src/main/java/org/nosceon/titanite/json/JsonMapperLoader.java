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
package org.nosceon.titanite.json;

import org.nosceon.titanite.json.impl.GsonMapper;
import org.nosceon.titanite.json.impl.JacksonMapper;

import java.util.Optional;

/**
 * @author Johan Siebens
 */
public class JsonMapperLoader {

    private static enum Singleton {

        INSTANCE;

        private final Optional<JsonMapper> mapper;

        private Singleton() {
            this.mapper = Optional.ofNullable(load());
        }

    }

    public static JsonMapper get() {
        return Singleton.INSTANCE.mapper.get();
    }

    private static JsonMapper load() {
        if (classIsAvailable("com.fasterxml.jackson.databind.ObjectMapper")) {
            return new JacksonMapper();
        }

        if (classIsAvailable("com.google.gson.Gson")) {
            return new GsonMapper();
        }

        return null;
    }

    private static boolean classIsAvailable(String name) {
        try {
            Class.forName(name);
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

}
