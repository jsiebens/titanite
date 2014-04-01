/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nosceon.titanite;

import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Johan Siebens
 */
@FunctionalInterface
public interface Filter<FI, FO, SI, SO> {

    default <NI, NO> Filter<FI, FO, NI, NO> andThen(Filter<? super SI, ? extends SO, NI, NO> next) {
        return (fi, f) -> apply(fi, (i) -> next.apply(i, f));
    }

    default Function<FI, FO> andThen(Function<? super SI, ? extends SO> next) {
        return (i) -> apply(i, next);
    }

    default Routings<FI, FO> andThen(Routings<? super SI, ? extends SO> routings) {
        Stream<Routing<FI, FO>> map = routings.get().stream().map(r -> new Routing<>(r.method(), r.pattern(), (i) -> apply(i, r.function())));
        return new Routings<>(map.collect(toList()));
    }

    FO apply(FI request, Function<? super SI, ? extends SO> function);

}
