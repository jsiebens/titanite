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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;

import static org.nosceon.titanite.HttpServerException.propagate;

public final class JacksonJsonMapper implements JsonMapper {

    private final ObjectMapper mapper;

    public JacksonJsonMapper() {
        this(new ObjectMapper());
    }

    public JacksonJsonMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T read(InputStream in, Class<T> type) {
        return propagate(() -> mapper.readValue(in, type));
    }

    @Override
    public void write(OutputStream out, Object value) {
        propagate(() -> {
            mapper.writeValue(out, value);
            return true;
        });
    }

}
