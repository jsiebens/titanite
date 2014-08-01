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

import org.nosceon.titanite.exception.InvalidCookieParamException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * @author Johan Siebens
 */
public final class CookieParams extends SingleParams {

    private final Map<String, CookieParam> cookies;

    CookieParams() {
        this(Collections.emptyMap());
    }

    CookieParams(Map<String, CookieParam> cookies) {
        this.cookies = cookies;
    }

    public CookieParam getCookie(String name) {
        return cookies.get(name);
    }

    @Override
    public String getString(String name) {
        return Optional.ofNullable(cookies.get(name)).map(CookieParam::value).orElse(null);
    }

    @Override
    public Set<String> keys() {
        return unmodifiableSet(cookies.keySet());
    }

    @Override
    protected IllegalArgumentException translate(Exception e, String type, String name, String value) {
        return new InvalidCookieParamException(e, type, name, value);
    }

}
