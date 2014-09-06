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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author Johan Siebens
 */
public final class EmptyBodyParser extends AbstractBodyParser {

    public EmptyBodyParser() {
        super(-1);
    }

    @Override
    public void initialize(ChannelHandlerContext ctx, HttpRequest request) {

    }

    @Override
    protected void doOffer(HttpContent chunk) {

    }

    @Override
    public void release() {

    }

    @Override
    protected Object apply(Class<?> aClass) {
        return null;
    }

}
