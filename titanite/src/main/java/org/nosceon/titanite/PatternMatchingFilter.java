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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * <p>Part of this mapping code has been kindly borrowed from <a href="http://ant.apache.org/">Apache Ant.</a></p>
 *
 * @author Johan Siebens
 */
public final class PatternMatchingFilter implements Filter {

    private final List<PathMatcher> includes = new LinkedList<>();

    private final List<PathMatcher> excludes = new LinkedList<>();

    private final BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter;

    public PatternMatchingFilter(BiFunction<Request, Function<Request, CompletionStage<Response>>, CompletionStage<Response>> filter) {
        this.filter = filter;
    }

    @Override
    public CompletionStage<Response> apply(Request request, Function<Request, CompletionStage<Response>> handler) {
        boolean excluded = isExcluded(request.path());
        boolean included = isIncluded(request.path());
        if (!excluded && included) {
            return filter.apply(request, handler);
        }
        else {
            return handler.apply(request);
        }
    }

    public PatternMatchingFilter include(String pattern, String... patterns) {
        this.includes.add(new PathMatcher(pattern));
        this.includes.addAll(ofNullable(patterns).map((String[] ps) -> Arrays.stream(ps).map(PathMatcher::new).collect(toList())).orElseGet(Collections::emptyList));
        return this;
    }

    public PatternMatchingFilter exclude(String pattern, String... patterns) {
        this.excludes.add(new PathMatcher(pattern));
        this.excludes.addAll(ofNullable(patterns).map((String[] ps) -> Arrays.stream(ps).map(PathMatcher::new).collect(toList())).orElseGet(Collections::emptyList));
        return this;
    }

    private boolean isIncluded(String path) {
        return includes.isEmpty() || isMatch(includes, path);
    }

    private boolean isExcluded(String path) {
        return !excludes.isEmpty() && isMatch(excludes, path);
    }

    private boolean isMatch(List<PathMatcher> matchers, String path) {
        return matchers.parallelStream().filter(p -> p.match(path)).findAny().isPresent();
    }

    private static class PathMatcher {

        public static final String PATH_SEPARATOR = "/";

        private final String pattern;

        public PathMatcher(String pattern) {
            this.pattern = pattern;
        }

        public boolean isPattern(String str) {
            return (str.indexOf('*') != -1 || str.indexOf('?') != -1);
        }

        public boolean match(String path) {
            if (path.startsWith(PATH_SEPARATOR) != pattern.startsWith(PATH_SEPARATOR)) {
                return false;
            }

            if (!isPattern(pattern)) {
                return pattern.equals(path);
            }
            else {

                String[] pattDirs = pattern.split(PATH_SEPARATOR);
                String[] pathDirs = path.split(PATH_SEPARATOR);

                int pattIdxStart = 0;
                int pattIdxEnd = pattDirs.length - 1;
                int pathIdxStart = 0;
                int pathIdxEnd = pathDirs.length - 1;

                // Match all elements up to the first **
                while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
                    String pattDir = pattDirs[pattIdxStart];
                    if ("**".equals(pattDir)) {
                        break;
                    }
                    if (!matchStrings(pattDir, pathDirs[pathIdxStart])) {
                        return false;
                    }
                    pattIdxStart++;
                    pathIdxStart++;
                }

                if (pathIdxStart > pathIdxEnd) {
                    // Path is exhausted, only match if rest of pattern is * or **'s
                    if (pattIdxStart > pattIdxEnd) {
                        return (pattern.endsWith(PATH_SEPARATOR) ? path.endsWith(PATH_SEPARATOR) :
                            !path.endsWith(PATH_SEPARATOR));
                    }
                    if (pattIdxStart == pattIdxEnd && pattDirs[pattIdxStart].equals("*") && path.endsWith(PATH_SEPARATOR)) {
                        return true;
                    }
                    for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
                        if (!pattDirs[i].equals("**")) {
                            return false;
                        }
                    }
                    return true;
                }
                else if (pattIdxStart > pattIdxEnd) {
                    // String not exhausted, but pattern is. Failure.
                    return false;
                }

                // up to last '**'
                while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
                    String pattDir = pattDirs[pattIdxEnd];
                    if (pattDir.equals("**")) {
                        break;
                    }
                    if (!matchStrings(pattDir, pathDirs[pathIdxEnd])) {
                        return false;
                    }
                    pattIdxEnd--;
                    pathIdxEnd--;
                }
                if (pathIdxStart > pathIdxEnd) {
                    // String is exhausted
                    for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
                        if (!pattDirs[i].equals("**")) {
                            return false;
                        }
                    }
                    return true;
                }

                while (pattIdxStart != pattIdxEnd && pathIdxStart <= pathIdxEnd) {
                    int patIdxTmp = -1;
                    for (int i = pattIdxStart + 1; i <= pattIdxEnd; i++) {
                        if (pattDirs[i].equals("**")) {
                            patIdxTmp = i;
                            break;
                        }
                    }
                    if (patIdxTmp == pattIdxStart + 1) {
                        // '**/**' situation, so skip one
                        pattIdxStart++;
                        continue;
                    }
                    // Find the pattern between padIdxStart & padIdxTmp in path between
                    // strIdxStart & strIdxEnd
                    int patLength = (patIdxTmp - pattIdxStart - 1);
                    int strLength = (pathIdxEnd - pathIdxStart + 1);
                    int foundIdx = -1;

                    strLoop:
                    for (int i = 0; i <= strLength - patLength; i++) {
                        for (int j = 0; j < patLength; j++) {
                            String subPat = pattDirs[pattIdxStart + j + 1];
                            String subStr = pathDirs[pathIdxStart + i + j];
                            if (!matchStrings(subPat, subStr)) {
                                continue strLoop;
                            }
                        }
                        foundIdx = pathIdxStart + i;
                        break;
                    }

                    if (foundIdx == -1) {
                        return false;
                    }

                    pattIdxStart = patIdxTmp;
                    pathIdxStart = foundIdx + patLength;
                }

                for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
                    if (!pattDirs[i].equals("**")) {
                        return false;
                    }
                }

                return true;
            }
        }

        private boolean matchStrings(String pattern, String str) {
            return new AntPathStringMatcher(pattern).matchStrings(str);
        }

        private static class AntPathStringMatcher {

            private static final Pattern GLOB_PATTERN = Pattern.compile("\\?|\\*");

            private final Pattern pattern;

            public AntPathStringMatcher(String pattern) {
                StringBuilder patternBuilder = new StringBuilder();
                Matcher m = GLOB_PATTERN.matcher(pattern);
                int end = 0;
                while (m.find()) {
                    patternBuilder.append(quote(pattern, end, m.start()));
                    String match = m.group();
                    if ("?".equals(match)) {
                        patternBuilder.append('.');
                    }
                    else if ("*".equals(match)) {
                        patternBuilder.append(".*");
                    }
                    end = m.end();
                }
                patternBuilder.append(quote(pattern, end, pattern.length()));
                this.pattern = Pattern.compile(patternBuilder.toString());
            }

            private String quote(String s, int start, int end) {
                if (start == end) {
                    return "";
                }
                return Pattern.quote(s.substring(start, end));
            }

            public boolean matchStrings(String str) {
                return this.pattern.matcher(str).matches();
            }

        }

    }

}
