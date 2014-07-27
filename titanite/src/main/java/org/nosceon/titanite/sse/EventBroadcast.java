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

import org.nosceon.titanite.Response;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Johan Siebens
 */
public class EventBroadcast extends SSE {

    private final List<Subscriber> subscribers = new LinkedList<>();

    private final Dispatcher dispatcher;

    public EventBroadcast() {
        this.dispatcher = new Dispatcher(ForkJoinPool.commonPool());
    }

    public final CompletionStage<Response> newSubscription() {
        return new Subscriber().toFuture();
    }

    protected void onSubscribe(String id) {

    }

    protected void onUnsubscribe(String id) {

    }

    protected final int nrOfSubscribers() {
        return subscribers.size();
    }

    @Override
    final void sendFormatted(String formatted) {
        dispatcher.execute(() -> {
            for (EventSource subscriber : subscribers) {
                subscriber.sendFormatted(formatted);
            }
        });
    }

    private void subscribe(Subscriber subscriber) {
        dispatcher.execute(() -> {
            subscribers.add(subscriber);
            onSubscribe(subscriber.id);
        });
    }

    private void unsubscribe(Subscriber subscriber) {
        dispatcher.execute(() -> {
            subscribers.remove(subscriber);
            onUnsubscribe(subscriber.id);
        });
    }

    private class Subscriber extends EventSource {

        private final String id;

        private Subscriber() {
            this.id = UUID.randomUUID().toString().replaceAll("-", "");
        }

        @Override
        public void onConnected() {
            subscribe(this);
            onDisconnected(() -> unsubscribe(this));
        }

    }

    public static class Dispatcher implements Executor {

        private static final int THROUGHPUT = 25;

        private final Executor executor;

        private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

        private final AtomicBoolean scheduled = new AtomicBoolean(false);

        public Dispatcher(Executor executor) {
            this.executor = executor;
        }

        @Override
        public void execute(Runnable command) {
            tasks.offer(command);
            trySchedule();
        }

        private void trySchedule() {
            if (!tasks.isEmpty()) {
                if (scheduled.compareAndSet(false, true)) {
                    executor.execute(this::processTasks);
                }
            }
        }

        private void processTasks() {
            try {
                int i = 0;
                while (!tasks.isEmpty() && i < THROUGHPUT) {
                    tasks.poll().run();
                    i++;
                }
            }
            finally {
                scheduled.compareAndSet(true, false);
                trySchedule();
            }
        }

    }

}
