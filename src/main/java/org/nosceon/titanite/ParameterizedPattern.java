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
import java.util.regex.Pattern;

/**
 * @author Johan Siebens
 */
final class ParameterizedPattern {

    private final String pattern;

    private final Pattern compiledPattern;

    private final Set<String> parameters;

    public ParameterizedPattern(String input) {
        final Data p = parse(input);
        this.pattern = p.pattern;
        this.parameters = p.parameters;
        this.compiledPattern = Pattern.compile(p.pattern);
    }

    public Matcher matcher(String path) {
        return this.parameters.isEmpty() ?
            new Equals(pattern, path) :
            new Default(compiledPattern.matcher(path), parameters);
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
        return parameters.equals(that.parameters) && pattern.equals(that.pattern);
    }

    @Override
    public int hashCode() {
        int result = pattern.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
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

            sb.append("/");

            if (n.startsWith(":")) {
                String g = n.substring(1);
                if (!groups.add(g)) {
                    throw new IllegalArgumentException("Cannot use identifier " + g + " more than once in pattern string");
                }
                sb.append("(?<").append(g).append(">[^\\/]+)");
            }
            else if (n.startsWith("*")) {
                if (segments.hasNext()) {
                    throw new IllegalArgumentException("Dynamic part over more than one segment should be placed at the end");
                }
                String g = n.substring(1);
                if (!groups.add(g)) {
                    throw new IllegalArgumentException("Cannot use identifier " + g + " more than once in pattern string");
                }
                sb.append("(?<").append(g).append(">.+)");
            }
            else {
                sb.append(n);
            }
        }

        return new Data(sb.toString(), groups);
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

        private final java.util.regex.Matcher matcher;

        private final Collection<String> parameters;

        public Default(java.util.regex.Matcher matcher, Collection<String> parameters) {
            this.matcher = matcher;
            this.parameters = parameters;
        }

        public boolean matches() {
            return matcher.matches();
        }

        public Map<String, String> parameters() {
            Map<String, String> result = new LinkedHashMap<>(parameters.size());
            for (String name : parameters) {
                result.put(name, matcher.group(name));
            }
            return result;
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
