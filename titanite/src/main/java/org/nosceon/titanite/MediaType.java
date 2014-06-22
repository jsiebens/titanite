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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Johan Siebens
 */
public final class MediaType {

    public static final String WILDCARD = "*";

    public final static MediaType APPLICATION_XML = valueOf("application/xml");

    public final static MediaType APPLICATION_ATOM_XML = valueOf("application/atom+xml");

    public final static MediaType APPLICATION_XHTML_XML = valueOf("application/xhtml+xml");

    public final static MediaType APPLICATION_SVG_XML = valueOf("application/svg+xml");

    public final static MediaType APPLICATION_JSON = valueOf("application/json");

    public final static MediaType APPLICATION_FORM_URLENCODED = valueOf("application/x-www-form-urlencoded");

    public final static MediaType MULTIPART_FORM_DATA = valueOf("multipart/form-data");

    public final static MediaType APPLICATION_OCTET_STREAM = valueOf("application/octet-stream");

    public final static MediaType TEXT_PLAIN = valueOf("text/plain");

    public final static MediaType TEXT_XML = valueOf("text/xml");

    public final static MediaType TEXT_HTML = valueOf("text/html");

    private final String type;

    private final String subType;

    private final Map<String, String> parameters;

    private MediaType(String type, String subType, Map<String, String> parameters) {
        this.type = type;
        this.subType = subType;
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    public String getParameter(String parameter) {
        return parameters.get(parameter);
    }

    @Override
    public String toString() {
        StringBuilder builder =
            new StringBuilder()
                .append(this.type)
                .append('/')
                .append(this.subType);

        this.parameters.entrySet().forEach(
            e -> builder
                .append(';')
                .append(e.getKey())
                .append('=')
                .append(e.getValue())
        );

        return builder.toString();
    }

    public static MediaType valueOf(String value) {
        String[] parts = value.split(";");
        Map<String, String> parameters = new HashMap<>();

        for (int i = 1; i < parts.length; ++i) {
            String p = parts[i];
            String[] subParts = p.split("=");
            if (subParts.length == 2) {
                parameters.put(subParts[0].trim(), subParts[1].trim());
            }
        }
        String fullType = parts[0].trim();

        if (fullType.equals(WILDCARD)) {
            fullType = WILDCARD + '/' + WILDCARD;
        }

        String[] types = fullType.split("/");

        return new MediaType(types[0].trim(), types[1].trim(), parameters);
    }

}
