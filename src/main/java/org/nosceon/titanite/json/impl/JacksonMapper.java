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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nosceon.titanite.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class JacksonMapper implements JsonMapper {

    private final ObjectMapper mapper;

    public JacksonMapper() {
        this(new ObjectMapper());
    }

    public JacksonMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T read(InputStream in, Class<T> type) throws IOException {
        return mapper.readValue(in, type);
    }

    @Override
    public void write(OutputStream out, Object value) throws IOException {
        mapper.writeValue(out, value);
    }

}
