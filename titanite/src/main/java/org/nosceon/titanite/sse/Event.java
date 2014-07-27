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

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

/**
 * @author Johan Siebens
 */
public final class Event {

    public static Event event(String data) {
        return new Event(null, null, data);
    }

    private String name;

    private String id;

    private String data;

    public Event(String name, String id, String data) {
        this.name = name;
        this.id = id;
        this.data = data;
    }

    public Event withName(String name) {
        this.name = name;
        return this;
    }

    public Event withId(String id) {
        this.id = id;
        return this;
    }

    public String formatted() {
        return formatted(id, name, data);
    }

    static String formatted(String id, String name, String data) {
        StringBuilder sb = new StringBuilder();
        ofNullable(name).ifPresent(n -> sb.append("event: ").append(n).append('\n'));
        ofNullable(id).ifPresent(n -> sb.append("id: ").append(n).append('\n'));
        stream(data.split("(\r?\n)|\r")).forEach(s -> sb.append("data: ").append(s).append('\n'));
        sb.append('\n');

        return sb.toString();
    }

}
