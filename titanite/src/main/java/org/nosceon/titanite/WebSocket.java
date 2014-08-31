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

import java.util.function.Consumer;

/**
 * @author Johan Siebens
 */
public interface WebSocket {

    interface Channel {

        void onTextMessage(Consumer<String> consumer);

        void onBinaryMessage(Consumer<byte[]> consumer);

        void onClose(Runnable action);

        void write(String message);

        void write(byte[] message);

        void close();

    }

    void onReady(Channel channel);

}
