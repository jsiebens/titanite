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
package org.nosceon.titanite.json.impl;

import com.google.gson.Gson;
import org.nosceon.titanite.StreamingInput;
import org.nosceon.titanite.StreamingOutput;
import org.nosceon.titanite.json.JsonMapper;

import java.io.*;

/**
 * @author Johan Siebens
 */
public final class GsonMapper implements JsonMapper {

    private final Gson gson;

    public GsonMapper() {
        this(defaultGson());
    }

    public GsonMapper(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> StreamingInput<T> in(Class<T> type) {
        return (in) -> {
            try (Reader r = new InputStreamReader(in)) {
                return gson.fromJson(r, type);
            }
        };
    }

    @Override
    public StreamingOutput out(Object value) {
        return (out) -> {
            try (Writer w = new BufferedWriter(new OutputStreamWriter(out))) {
                gson.toJson(value, w);
            }
        };
    }

    private static Gson defaultGson() {
        return new Gson();
    }

}
