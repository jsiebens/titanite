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

import org.nosceon.titanite.SingleParams;

/**
 * @author Johan Siebens
 */
public abstract class Scope extends SingleParams {

    public abstract void set(String key, String value);

    public final void set(String key, short value) {
        set(key, String.valueOf(value));
    }

    public final void set(String key, int value) {
        set(key, String.valueOf(value));
    }

    public final void set(String key, long value) {
        set(key, String.valueOf(value));
    }

    public final void set(String key, float value) {
        set(key, String.valueOf(value));
    }

    public final void set(String key, double value) {
        set(key, String.valueOf(value));
    }

    public final void set(String key, boolean value) {
        set(key, String.valueOf(value));
    }

}
