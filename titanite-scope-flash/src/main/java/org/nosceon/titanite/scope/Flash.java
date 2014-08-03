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

import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;
import org.nosceon.titanite.Scope;
import org.nosceon.titanite.exception.FlashNotAvailableException;
import org.nosceon.titanite.scope.exception.InvalidFlashParamException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.Utils.checkNotNull;

/**
 * @author Johan Siebens
 */
public final class Flash extends Scope {

    public static final String ATTRIBUTE_ID = Flash.class.getName();

    public static Scope flash(Request request) {
        Flash flash = request.attributes().get(ATTRIBUTE_ID);
        return checkNotNull(flash, () -> new FlashNotAvailableException("Flash not available, a " + FlashFilter.class.getName() + " should be registered to use a Flash"));
    }

    public static BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> enableFlash() {
        return new FlashFilter();
    }

    public static BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> enableFlash(String cookieName) {
        return new FlashFilter(cookieName);
    }

    private final Map<String, String> previous = new HashMap<>();

    private final Map<String, String> current = new ConcurrentHashMap<>();

    Flash() {

    }

    Flash(Map<String, String> values) {
        this.previous.putAll(values);
    }

    @Override
    public void set(String key, String value) {
        this.current.compute(key, (k, v) -> value);
    }

    @Override
    public String getString(String name) {
        return ofNullable(previous.get(name)).orElseGet(() -> current.get(name));
    }

    @Override
    public Set<String> keys() {
        Set<String> keys = new HashSet<>();
        keys.addAll(previous.keySet());
        keys.addAll(current.keySet());
        return unmodifiableSet(keys);
    }

    @Override
    public void clear() {
        this.previous.clear();
        this.current.clear();
    }

    @Override
    protected IllegalArgumentException translate(Exception e, String type, String name, String value) {
        return new InvalidFlashParamException(e, type, name, value);
    }

    Map<String, String> values() {
        return current;
    }

}
