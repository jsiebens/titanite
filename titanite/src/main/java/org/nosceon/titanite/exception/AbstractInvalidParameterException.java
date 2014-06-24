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
package org.nosceon.titanite.exception;

/**
 * @author Johan Siebens
 */
public abstract class AbstractInvalidParameterException extends IllegalArgumentException {

    private final String type;

    private final String name;

    private final String value;

    protected AbstractInvalidParameterException(Throwable cause, String paramType, String type, String name, String value) {
        super("Invalid " + paramType + " parameter '" + name + "': '" + value + "' cannot be converted to " + type, cause);

        this.type = type;
        this.name = name;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
