package org.nosceon.titanite;

import java.util.function.Function;

/**
 * @author Johan Siebens
 */
interface Params {

    Function<String, Short> SHORT = Short::valueOf;

    Function<String, Integer> INT = Integer::valueOf;

    Function<String, Long> LONG = Long::valueOf;

    Function<String, Float> FLOAT = Float::valueOf;

    Function<String, Double> DOUBLE = Double::valueOf;

    Function<String, Boolean> BOOLEAN = s -> s.equals("1") || s.equals("t") || s.equals("true") || s.equals("on");
    
}
