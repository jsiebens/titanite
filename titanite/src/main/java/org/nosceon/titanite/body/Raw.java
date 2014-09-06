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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.nosceon.titanite.BodyReader;

import java.util.concurrent.Callable;

import static org.nosceon.titanite.Utils.callUnchecked;

/**
 * @author Johan Siebens
 */
public final class Raw {

    private ByteBuf content;

    Raw(ByteBuf content) {
        this.content = content;
    }

    public <T> T map(BodyReader<T> reader) {
        if (content.readableBytes() > 0) {
            Callable<T> callable = () -> reader.readFrom(new ByteBufInputStream(content));
            return callUnchecked(callable);
        }
        else {
            return null;
        }
    }

}
