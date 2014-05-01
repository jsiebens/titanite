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
