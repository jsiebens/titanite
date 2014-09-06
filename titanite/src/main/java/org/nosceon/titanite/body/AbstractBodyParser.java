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

import io.netty.handler.codec.http.HttpContent;
import org.nosceon.titanite.BodyReader;

import java.io.InputStream;
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public abstract class AbstractBodyParser implements BodyParser {

    private long maxRequestSize;

    private long currentRequestSize;

    private boolean exceeded = false;

    protected AbstractBodyParser(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    @Override
    public final void offer(HttpContent chunk) {
        int chunkSize = chunk.content().readableBytes();

        if ((maxRequestSize != -1) && (currentRequestSize > maxRequestSize - chunkSize)) {
            exceeded = true;
            release();
        }

        if (!exceeded) {
            doOffer(chunk);
        }

        currentRequestSize += chunkSize;
    }

    @Override
    public final long size() {
        return currentRequestSize;
    }

    @Override
    public final boolean isMaximumExceeded() {
        return exceeded;
    }

    @Override
    public final Body body() {
        return new InternalBody(this::apply);
    }

    protected abstract Object apply(Class<?> aClass);

    protected abstract void doOffer(HttpContent chunk);

    private static class InternalBody implements Body {

        private final Function<Class<?>, ?> function;

        InternalBody(Function<Class<?>, ?> function) {
            this.function = function;
        }

        public final <T> T as(Class<T> type) {
            return (T) function.apply(type);
        }

        public final <T> T as(BodyReader<T> reader) {
            return as(Raw.class).map(reader);
        }

        public final FormParams asForm() {
            return as(FormParams.class);
        }

        public final InputStream asStream() {
            return as(InputStream.class);
        }

        public final String asText() {
            return as(String.class);
        }

    }

}
