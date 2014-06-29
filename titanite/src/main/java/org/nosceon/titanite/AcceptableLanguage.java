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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * @author Johan Siebens
 */
public final class AcceptableLanguage {

    private static final String WILDCARD = "*";

    public static final AcceptableLanguage ANY = valueOf(WILDCARD);

    private final String primaryTag;

    private final String subTags;

    private final double quality;

    private AcceptableLanguage(String primaryTag, String subTags, double quality) {
        this.primaryTag = primaryTag;
        this.subTags = subTags;
        this.quality = quality;
    }

    public String getPrimaryTag() {
        return primaryTag;
    }

    public String getSubTags() {
        return subTags;
    }

    public double getQuality() {
        return quality;
    }

    public Locale asLocale() {
        return (subTags == null)
            ? new Locale(primaryTag)
            : new Locale(primaryTag, subTags);
    }

    public boolean includes(Locale tag) {
        if (tag == null) {
            return false;
        }

        if (WILDCARD.equals(primaryTag)) {
            return true;
        }

        if (subTags == null) {
            return primaryTag.equalsIgnoreCase(tag.getLanguage());
        }
        else {
            return primaryTag.equalsIgnoreCase(tag.getLanguage()) && subTags.equalsIgnoreCase(tag.getCountry());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AcceptableLanguage that = (AcceptableLanguage) o;

        if (Double.compare(that.quality, quality) != 0) {
            return false;
        }
        if (!primaryTag.equals(that.primaryTag)) {
            return false;
        }
        if (subTags != null ? !subTags.equals(that.subTags) : that.subTags != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = primaryTag.hashCode();
        result = 31 * result + (subTags != null ? subTags.hashCode() : 0);
        temp = Double.doubleToLongBits(quality);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        if (isWildcardType()) {
            return WILDCARD;
        }
        else {
            if (subTags != null) {
                return primaryTag + '-' + subTags + ";q=" + quality;
            }
            else {
                return primaryTag + ";q=" + quality;
            }
        }
    }

    private boolean isWildcardType() {
        return WILDCARD.equals(primaryTag);
    }

    public static List<AcceptableLanguage> valuesOf(String values) {
        return asList(values.split(",\\s*")).stream().map(AcceptableLanguage::valueOf).sorted(COMPARATOR).collect(toList());
    }

    public static AcceptableLanguage valueOf(String value) {
        if (WILDCARD.equals(value)) {
            return new AcceptableLanguage(WILDCARD, null, 1d);
        }
        else {
            String[] parts = value.split(";\\s*");

            if (parts.length > 2) {
                throw new IllegalArgumentException("'" + value + "' is not a valid language tag");
            }

            String tag = parts[0].trim();

            if (!isValid(tag)) {
                throw new IllegalArgumentException("'" + value + "' is not a valid language tag");
            }

            String primaryTag = tag;
            String subTag = null;

            int subIndex = tag.indexOf('-');
            if (subIndex != -1) {
                primaryTag = tag.substring(0, subIndex);
                subTag = tag.substring(subIndex + 1, tag.length());
            }

            double q = 1d;

            if (parts.length > 1) {
                String parameter = parts[1].trim();
                int eqIndex = parameter.indexOf('=');
                if (eqIndex != -1) {
                    String attribute = parameter.substring(0, eqIndex);
                    if (!"q".equals(attribute)) {
                        throw new IllegalArgumentException("'" + value + "' is not a valid language tag");
                    }
                    q = Double.valueOf(parameter.substring(eqIndex + 1, parameter.length()));
                    checkQuality(q);
                }
            }

            return new AcceptableLanguage(primaryTag, subTag, q);
        }
    }

    private static boolean isValid(String tag) {
        int count = 0;
        for (int i = 0; i < tag.length(); i++) {
            final char c = tag.charAt(i);
            if (c == '-') {
                if (count == 0) {
                    return false;
                }
                count = 0;
            }
            else if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) {
                count++;
                if (count > 8) {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        return (count != 0);
    }

    private static void checkQuality(double q) {
        if (q < 0D || 1D < q) {
            throw new IllegalArgumentException("Invalid quality value \"" + q + "\": should be between 0.0 and 1.0");
        }
    }

    public static Locale bestCandidate(List<AcceptableLanguage> ranges, Collection<Locale> candidates) {
        List<AcceptableLanguage> sorted = ranges.stream().sorted(COMPARATOR).collect(toList());
        return
            candidates
                .stream()
                .map(m -> new Candidate(index(sorted, m), m))
                .filter(c -> c.index != Integer.MAX_VALUE)
                .sorted()
                .findFirst()
                .map(c -> c.locale)
                .orElse(null);
    }

    public static boolean accepts(List<AcceptableLanguage> ranges, Locale candidate) {
        return bestCandidate(ranges, Collections.singletonList(candidate)) != null;
    }

    private static int index(List<AcceptableLanguage> languages, Locale candidate) {
        for (int i = 0; i < languages.size(); i++) {
            AcceptableLanguage type = languages.get(i);
            if (type.includes(candidate)) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    private static final Comparator<AcceptableLanguage> COMPARATOR = new AcceptableLanguageComparator();

    private static class Candidate implements Comparable<Candidate> {

        public int index;

        public Locale locale;

        private Candidate(int index, Locale locale) {
            this.index = index;
            this.locale = locale;
        }

        @Override
        public int compareTo(Candidate o) {
            return index - o.index;
        }

    }

    private static class AcceptableLanguageComparator implements Comparator<AcceptableLanguage> {

        @Override
        public int compare(AcceptableLanguage acceptableLanguage1, AcceptableLanguage acceptableLanguage2) {
            double quality1 = acceptableLanguage1.getQuality();
            double quality2 = acceptableLanguage2.getQuality();
            int qualityComparison = Double.compare(quality2, quality1);
            if (qualityComparison != 0) {
                return qualityComparison;
            }
            else if (acceptableLanguage1.isWildcardType() && !acceptableLanguage2.isWildcardType()) {
                return 1;
            }
            else if (acceptableLanguage2.isWildcardType() && !acceptableLanguage1.isWildcardType()) {
                return -1;
            }
            else {
                return 0;
            }
        }

    }

}
