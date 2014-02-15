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
