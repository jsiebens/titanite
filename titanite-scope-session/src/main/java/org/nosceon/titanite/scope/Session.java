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
package org.nosceon.titanite.scope;

import org.nosceon.titanite.Filter;
import org.nosceon.titanite.Request;
import org.nosceon.titanite.Scope;
import org.nosceon.titanite.exception.InvalidSessionParamException;
import org.nosceon.titanite.exception.SessionNotAvailableException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.nosceon.titanite.Utils.checkNotNull;

/**
 * @author Johan Siebens
 */
public final class Session extends Scope {

    public static final String ATTRIBUTE_ID = Session.class.getName();

    public static Scope session(Request request) {
        Session session = request.attributes().get(ATTRIBUTE_ID);
        return checkNotNull(session, () -> new SessionNotAvailableException("Session not available, a " + SessionFilter.class.getName() + " should be registered to use a Session"));
    }

    public static Filter enableSessions(String secret) {
        return new SessionFilter(secret);
    }

    public static Filter enableSessions(String cookieName, String secret) {
        return new SessionFilter(cookieName, secret);
    }

    private final Map<String, String> values = new ConcurrentHashMap<>();

    Session() {

    }

    Session(Map<String, String> v) {
        this.values.putAll(v);
    }

    @Override
    public void set(String key, String value) {
        this.values.compute(key, (k, v) -> value);
    }

    @Override
    public String getString(String name) {
        return values.get(name);
    }

    @Override
    protected IllegalArgumentException translate(Exception e, String type, String name, String value) {
        return new InvalidSessionParamException(e, type, name, value);
    }

    Map<String, String> values() {
        return values;
    }

}

