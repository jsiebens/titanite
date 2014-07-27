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

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

/**
 * @author Johan Siebens
 */
final class ParameterizedPattern {

    private static final Pattern PARAM_NAME_PATTERN = Pattern.compile("[a-zA-Z][0-9a-zA-Z]*");

    private final String pattern;

    private final Function<String, Matcher> supplier;

    public ParameterizedPattern(String input) {
        this.pattern = input;
        this.supplier = createMatcherSupplier(input);
    }

    public boolean matches(String path) {
        return matcher(path).matches();
    }

    public Matcher matcher(String path) {
        return supplier.apply(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParameterizedPattern that = (ParameterizedPattern) o;

        return pattern.equals(that.pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    @Override
    public String toString() {
        return pattern;
    }

    private static Function<String, Matcher> createMatcherSupplier(String input) {
        Data p = parse(input);

        if (p.parameters.isEmpty()) {
            return s -> new Equals(p.pattern, s);
        }
        else {
            Pattern compiledPattern = Pattern.compile(p.pattern);
            return s -> new Default(compiledPattern.matcher(s), p.parameters);
        }
    }

    private static Data parse(String input) {
        String p = input;

        if (p.startsWith("/")) {
            p = p.substring(1);
        }

        Set<String> groups = new HashSet<>();
        Iterator<String> segments = Arrays.asList(p.split("/")).iterator();
        StringBuilder sb = new StringBuilder();

        while (segments.hasNext()) {
            String n = segments.next();

            if (n.startsWith(":")) {
                String g = validateName(n.substring(1));
                if (!groups.add(g)) {
                    throw new IllegalArgumentException("Cannot use identifier '" + g + "' more than once in pattern string");
                }
                sb.append('/').append("(?<").append(g).append(">[^\\/]+)");
            }
            else if (n.startsWith("*")) {
                if (segments.hasNext()) {
                    throw new IllegalArgumentException("Dynamic part over more than one segment should be placed at the end");
                }
                String g = validateName(n.substring(1));
                if (!groups.add(g)) {
                    throw new IllegalArgumentException("Cannot use identifier '" + g + "' more than once in pattern string");
                }
                sb.append("(\\/(?<").append(g).append(">.*))?");
            }
            else if (n.startsWith("+")) {
                if (segments.hasNext()) {
                    throw new IllegalArgumentException("Dynamic part over more than one segment should be placed at the end");
                }
                String g = validateName(n.substring(1));
                if (!groups.add(g)) {
                    throw new IllegalArgumentException("Cannot use identifier '" + g + "' more than once in pattern string");
                }
                sb.append('/').append("(?<").append(g).append(">.+)");
            }
            else {
                sb.append('/').append(n);
            }
        }

        return new Data(sb.toString(), groups);
    }

    private static String validateName(String n) {
        if (!PARAM_NAME_PATTERN.matcher(n).matches()) {
            throw new IllegalArgumentException("Invalid identifier '" + n + "', it does not match [a-zA-Z][0-9a-zA-Z]*");
        }
        return n;
    }

    private static class Data {

        private final String pattern;

        private final Set<String> parameters;

        private Data(String pattern, Set<String> parameters) {
            this.pattern = pattern;
            this.parameters = parameters;
        }

    }

    static interface Matcher {

        boolean matches();

        Map<String, String> parameters();

    }

    private static class Default implements Matcher {

        private final boolean matches;

        private Map<String, String> parameters = new LinkedHashMap<>();

        public Default(java.util.regex.Matcher matcher, Collection<String> parameters) {
            this.matches = matcher.matches();
            if (this.matches()) {
                for (String name : parameters) {
                    this.parameters.put(name, ofNullable(matcher.group(name)).orElse(""));
                }
            }
        }

        public boolean matches() {
            return matches;
        }

        public Map<String, String> parameters() {
            return parameters;
        }

    }

    private static class Equals implements Matcher {

        private final String command;

        private final String value;

        public Equals(String value, String command) {
            this.value = value;
            this.command = command;
        }

        @Override
        public boolean matches() {
            return value.equals(command);
        }

        @Override
        public Map<String, String> parameters() {
            return Collections.emptyMap();
        }

    }

}
