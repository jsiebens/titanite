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
package org.nosceon.titanite.body;

import io.netty.buffer.ByteBufInputStream;
import org.nosceon.titanite.Utils;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.nosceon.titanite.Utils.callUnchecked;

/**
 * @author Johan Siebens
 */
public final class RawBodyParser extends AbstractRawBodyParser {

    public RawBodyParser(long maxRequestSize) {
        super(maxRequestSize);
    }

    @Override
    protected Object apply(Class<?> type) {

        if (type.isAssignableFrom(Raw.class)) {
            return new Raw(content());
        }

        if (type.isAssignableFrom(InputStream.class)) {
            return callUnchecked(this::stream);
        }

        if (type.isAssignableFrom(String.class)) {
            return callUnchecked(() -> Utils.toString(new InputStreamReader(stream())));
        }

        throw new IllegalArgumentException(this.getClass().getName() + " does not support [" + type.getName() + "]");
    }

    private ByteBufInputStream stream() {
        return new ByteBufInputStream(content());
    }

    public static void main(String[] args) {


    }

}
