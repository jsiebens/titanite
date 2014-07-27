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

import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.Test;
import org.nosceon.titanite.sse.EventBroadcast;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.nosceon.titanite.sse.Event.event;

/**
 * @author Johan Siebens
 */
public class ServerSentEventsE2ETest extends AbstractE2ETest {

    private EventBroadcast sse = new EventBroadcast();

    @Override
    protected Shutdownable configureAndStartHttpServer(HttpServer server) throws Exception {
        return
            server
                .register(Method.GET, "/events", req -> sse.newSubscription())
                .start();
    }

    @Test
    public void test() throws Exception {
        Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
        WebTarget target = client.target(uri("/events"));

        List<InboundEvent> events = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        EventSource eventSource = EventSource.target(target).build();
        EventListener listener = inboundEvent -> {
            events.add(inboundEvent);
            latch.countDown();
        };
        eventSource.register(listener);
        eventSource.open();

        sse.send(event("Hello 1\nLine 1").withId("1").withName("hello1"));
        sse.send(event("Hello 2\nLine 2").withId("2").withName("hello2"));
        sse.send(event("Hello 3\nLine 3").withId("3").withName("hello3"));

        assertTrue(latch.await(500, TimeUnit.MILLISECONDS));

        assertThat(events.get(1).getId(), equalTo("2"));
        assertThat(events.get(1).getName(), equalTo("hello2"));
        assertThat(events.get(1).readData(), equalTo("Hello 2\nLine 2"));
    }

}
