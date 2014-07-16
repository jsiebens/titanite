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
package org.nosceon.titanite.json;

import org.nosceon.titanite.BodyReader;
import org.nosceon.titanite.BodyWriter;

/**
 * @author Johan Siebens
 */
public final class Json {

    public static <T> BodyReader<T> json(Class<T> type) {
        return JsonMapperLoader.get().in(type);
    }

    public static BodyWriter json(Object value) {
        return JsonMapperLoader.get().out(value);
    }

}