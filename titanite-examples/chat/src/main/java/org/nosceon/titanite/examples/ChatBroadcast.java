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
package org.nosceon.titanite.examples;

import org.nosceon.titanite.WebSocket;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Johan Siebens
 */
public class ChatBroadcast {

    private final List<Subscriber> subscribers = new LinkedList<>();

    private final Dispatcher dispatcher;

    public ChatBroadcast() {
        this.dispatcher = new Dispatcher(ForkJoinPool.commonPool());
    }

    public WebSocket newSubscriber(String nick) {
        return new Subscriber(nick);
    }

    private void publishNewMessage(String nick, String message) {
        dispatcher.execute(() -> subscribers.forEach(s -> s.sendMessage(nick, message)));
    }

    private void subscribe(Subscriber subscriber) {
        dispatcher.execute(() -> {
            subscribers.forEach(s -> subscriber.sendAddUser(s.nickName));
            subscribers.add(subscriber);
            subscribers.forEach(s -> s.sendAddUser(subscriber.nickName));
        });
    }

    private void unsubscribe(Subscriber subscriber) {
        dispatcher.execute(() -> {
            subscribers.remove(subscriber);
            subscribers.forEach(s -> s.sendRemoveUser(subscriber.nickName));
        });
    }

    private class Subscriber implements WebSocket {

        private String nickName;

        private Channel channel;

        private Subscriber(String nickName) {
            this.nickName = nickName;
        }

        @Override
        public void onReady(Channel channel) {
            this.channel = channel;
            subscribe(this);
            channel.onTextMessage(s -> publishNewMessage(nickName, s));
            channel.onClose(() -> unsubscribe(this));
        }

        private void sendAddUser(String nickName) {
            this.channel.write("{\"addUser\":\"" + nickName + "\"}");
        }

        private void sendRemoveUser(String nickName) {
            this.channel.write("{\"removeUser\":\"" + nickName + "\"}");
        }

        private void sendMessage(String nick, String message) {
            this.channel.write("{\"nickname\":\"" + nick + "\", \"message\":\"" + message.replace("\"", "\\\"") + "\"}");
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
