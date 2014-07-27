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
package org.nosceon.titanite.sse;

import org.nosceon.titanite.ChunkedOutput;
import org.nosceon.titanite.MediaType;
import org.nosceon.titanite.Response;

import java.util.concurrent.CompletionStage;

import static org.nosceon.titanite.Response.ok;

/**
 * @author Johan Siebens
 */
public abstract class EventSource extends SSE implements ChunkedOutput {

    public static final MediaType EVENT_STREAM = MediaType.valueOf("text/event-stream");

    private Channel channel;

    @Override
    public final void onReady(Channel channel) {
        this.channel = channel;
        onConnected();
    }

    public abstract void onConnected();

    public void onDisconnected(Runnable runnable) {
        channel.onDisconnect(runnable);
    }

    @Override
    void sendFormatted(String formatted) {
        channel.write(formatted.getBytes());
    }

    public final Response toResponse() {
        return ok().type(EVENT_STREAM).chunks(this);
    }

    public final CompletionStage<Response> toFuture() {
        return toResponse().toFuture();
    }

}
