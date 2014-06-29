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

import java.nio.charset.Charset;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * <p>Part of this code has been kindly borrowed from <a href="https://github.com/spring-projects/spring-framework">Spring Framework</a>.
 *
 * @author Johan Siebens
 */
public final class MediaType {

    public static final MediaType ANY;

    public static final MediaType APPLICATION_XML;

    public static final MediaType APPLICATION_JSON;

    public static final MediaType APPLICATION_FORM_URLENCODED;

    public static final MediaType MULTIPART_FORM_DATA;

    public static final MediaType TEXT_PLAIN;

    public static final MediaType TEXT_XML;

    public static final MediaType TEXT_HTML;

    private static final String WILDCARD_TYPE = "*";

    private static final String PARAM_CHARSET = "charset";

    private static final String PARAM_QUALITY_FACTOR = "q";

    private static final BitSet TOKEN;

    private final String type;

    private final String subtype;

    private final Map<String, String> parameters;

    static {
        // variable names refer to RFC 2616, section 2.2
        BitSet ctl = new BitSet(128);
        for (int i = 0; i <= 31; i++) {
            ctl.set(i);
        }
        ctl.set(127);

        BitSet separators = new BitSet(128);
        separators.set('(');
        separators.set(')');
        separators.set('<');
        separators.set('>');
        separators.set('@');
        separators.set(',');
        separators.set(';');
        separators.set(':');
        separators.set('\\');
        separators.set('\"');
        separators.set('/');
        separators.set('[');
        separators.set(']');
        separators.set('?');
        separators.set('=');
        separators.set('{');
        separators.set('}');
        separators.set(' ');
        separators.set('\t');

        TOKEN = new BitSet(128);
        TOKEN.set(0, 128);
        TOKEN.andNot(ctl);
        TOKEN.andNot(separators);
    }

    static {
        ANY = valueOf("*/*");
        APPLICATION_XML = valueOf("application/xml");
        APPLICATION_JSON = valueOf("application/json");
        APPLICATION_FORM_URLENCODED = valueOf("application/x-www-form-urlencoded");
        MULTIPART_FORM_DATA = valueOf("multipart/form-data");
        TEXT_PLAIN = valueOf("text/plain");
        TEXT_XML = valueOf("text/xml");
        TEXT_HTML = valueOf("text/html");
    }

    public MediaType(String type, String subtype) {
        this(type, subtype, null);
    }

    public MediaType(String type, String subtype, Map<String, String> parameters) {
        checkToken(type);
        checkToken(subtype);
        this.type = type.toLowerCase(Locale.ENGLISH);
        this.subtype = subtype.toLowerCase(Locale.ENGLISH);
        if (parameters != null && !parameters.isEmpty()) {
            Map<String, String> m = new LinkedHashMap<>(parameters.size());
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String attribute = entry.getKey();
                String value = entry.getValue();
                checkParameters(attribute, value);
                m.put(attribute.toLowerCase(Locale.ENGLISH), value);
            }
            this.parameters = Collections.unmodifiableMap(m);
        }
        else {
            this.parameters = Collections.emptyMap();
        }
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    public double getQuality() {
        return ofNullable(getParameter(PARAM_QUALITY_FACTOR)).map(Double::parseDouble).orElse(1D);
    }

    public boolean isWildcardType() {
        return WILDCARD_TYPE.equals(type);
    }

    public boolean isWildcardSubtype() {
        return WILDCARD_TYPE.equals(subtype) || subtype.startsWith("*+");
    }

    public boolean includes(MediaType other) {
        if (other == null) {
            return false;
        }
        if (this.isWildcardType()) {
            // */* includes anything
            return true;
        }
        else if (type.equals(other.type)) {
            if (subtype.equals(other.subtype)) {
                return true;
            }
            if (this.isWildcardSubtype()) {
                // wildcard with suffix, e.g. application/*+xml
                int thisPlusIdx = subtype.indexOf('+');
                if (thisPlusIdx == -1) {
                    return true;
                }
                else {
                    // application/*+xml includes application/soap+xml
                    int otherPlusIdx = other.subtype.indexOf('+');
                    if (otherPlusIdx != -1) {
                        String thisSubtypeNoSuffix = subtype.substring(0, thisPlusIdx);
                        String thisSubtypeSuffix = subtype.substring(thisPlusIdx + 1);
                        String otherSubtypeSuffix = other.subtype.substring(otherPlusIdx + 1);
                        if (thisSubtypeSuffix.equals(otherSubtypeSuffix) && WILDCARD_TYPE.equals(thisSubtypeNoSuffix)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MediaType)) {
            return false;
        }
        MediaType otherType = (MediaType) other;
        return (this.type.equalsIgnoreCase(otherType.type) &&
            this.subtype.equalsIgnoreCase(otherType.subtype) &&
            this.parameters.equals(otherType.parameters));
    }

    @Override
    public int hashCode() {
        int result = this.type.hashCode();
        result = 31 * result + this.subtype.hashCode();
        result = 31 * result + this.parameters.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder =
            new StringBuilder()
                .append(this.type)
                .append('/')
                .append(this.subtype);

        this.parameters.entrySet().forEach(
            e -> builder
                .append(';')
                .append(e.getKey())
                .append('=')
                .append(e.getValue())
        );

        return builder.toString();
    }

    private void checkToken(String token) {
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (!TOKEN.get(ch)) {
                throw new IllegalArgumentException("Invalid token character '" + ch + "' in token \"" + token + "\"");
            }
        }
    }

    private void checkParameters(String attribute, String value) {
        checkToken(attribute);
        if (PARAM_CHARSET.equals(attribute)) {
            value = unquote(value);
            Charset.forName(value);
        }
        if (PARAM_QUALITY_FACTOR.equals(attribute)) {
            value = unquote(value);
            double d = Double.parseDouble(value);
            if (d < 0D || 1D < d) {
                throw new IllegalArgumentException("Invalid quality value \"" + value + "\": should be between 0.0 and 1.0");
            }
        }
        else if (!isQuotedString(value)) {
            checkToken(value);
        }
    }

    private boolean isQuotedString(String s) {
        return s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")));
    }

    private String unquote(String s) {
        return s == null ? null : isQuotedString(s) ? s.substring(1, s.length() - 1) : s;
    }

    public static MediaType valueOf(String mediaType) {
        String[] parts = mediaType.split(";\\s*");

        String fullType = parts[0].trim();
        // java.net.HttpURLConnection returns a *; q=.2 Accept header
        if (WILDCARD_TYPE.equals(fullType)) {
            fullType = "*/*";
        }
        int subIndex = fullType.indexOf('/');
        if (subIndex == -1) {
            throw new IllegalArgumentException(mediaType + " does not contain '/'");
        }
        if (subIndex == fullType.length() - 1) {
            throw new IllegalArgumentException(mediaType + " does not contain subtype after '/'");
        }
        String type = fullType.substring(0, subIndex);
        String subtype = fullType.substring(subIndex + 1, fullType.length());
        if (WILDCARD_TYPE.equals(type) && !WILDCARD_TYPE.equals(subtype)) {
            throw new IllegalArgumentException(mediaType + " wildcard type is legal only in '*/*' (all mime types)");
        }

        Map<String, String> parameters = new LinkedHashMap<>();
        if (parts.length > 1) {
            parameters = new LinkedHashMap<>(parts.length - 1);
            for (int i = 1; i < parts.length; i++) {
                String parameter = parts[i];
                int eqIndex = parameter.indexOf('=');
                if (eqIndex != -1) {
                    String attribute = parameter.substring(0, eqIndex);
                    String value = parameter.substring(eqIndex + 1, parameter.length());
                    parameters.put(attribute, value);
                }
            }
        }

        return new MediaType(type, subtype, parameters);
    }

    public static MediaType bestCandidate(List<MediaType> ranges, Collection<MediaType> candidates) {
        List<MediaType> sorted = ranges.stream().sorted(COMPARATOR).collect(toList());
        return
            candidates
                .stream()
                .map(m -> new Candidate(index(sorted, m), m))
                .filter(c -> c.index != Integer.MAX_VALUE)
                .sorted()
                .findFirst()
                .map(c -> c.mediaType)
                .orElse(null);
    }

    public static boolean accepts(List<MediaType> ranges, MediaType candidate) {
        return bestCandidate(ranges, Collections.singletonList(candidate)) != null;
    }

    private static int index(List<MediaType> types, MediaType candidate) {
        for (int i = 0; i < types.size(); i++) {
            MediaType type = types.get(i);
            if (type.includes(candidate)) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    public static List<MediaType> valuesOf(String values) {
        return asList(values.split(",\\s*")).stream().map(MediaType::valueOf).collect(toList());
    }

    private static final Comparator<MediaType> COMPARATOR = new MediaTypeComparator();

    private static class Candidate implements Comparable<Candidate> {

        public int index;

        public MediaType mediaType;

        private Candidate(int index, MediaType mediaType) {
            this.index = index;
            this.mediaType = mediaType;
        }

        @Override
        public int compareTo(Candidate o) {
            return index - o.index;
        }

    }

    private static class MediaTypeComparator implements Comparator<MediaType> {

        @Override
        public int compare(MediaType mediaType1, MediaType mediaType2) {
            double quality1 = mediaType1.getQuality();
            double quality2 = mediaType2.getQuality();
            int qualityComparison = Double.compare(quality2, quality1);
            if (qualityComparison != 0) {
                return qualityComparison;  // audio/*;q=0.7 < audio/*;q=0.3
            }
            else if (mediaType1.isWildcardType() && !mediaType2.isWildcardType()) { // */* < audio/*
                return 1;
            }
            else if (mediaType2.isWildcardType() && !mediaType1.isWildcardType()) { // audio/* > */*
                return -1;
            }
            else if (!mediaType1.getType().equals(mediaType2.getType())) { // audio/basic == text/html
                return 0;
            }
            else { // mediaType1.getType().equals(mediaType2.getType())
                if (mediaType1.isWildcardSubtype() && !mediaType2.isWildcardSubtype()) { // audio/* < audio/basic
                    return 1;
                }
                else if (mediaType2.isWildcardSubtype() && !mediaType1.isWildcardSubtype()) { // audio/basic > audio/*
                    return -1;
                }
                else if (!mediaType1.getSubtype().equals(mediaType2.getSubtype())) { // audio/basic == audio/wave
                    return 0;
                }
                else {
                    int paramsSize1 = mediaType1.getParameters().size();
                    int paramsSize2 = mediaType2.getParameters().size();
                    return (paramsSize2 < paramsSize1 ? -1 : (paramsSize2 == paramsSize1 ? 0 : 1)); // audio/basic;level=1 < audio/basic
                }
            }
        }

    }

}
