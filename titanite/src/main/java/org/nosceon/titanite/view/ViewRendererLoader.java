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
package org.nosceon.titanite.view;


import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * @author Johan Siebens
 */
public class ViewRendererLoader {

    private static enum Singleton {

        INSTANCE;

        private final Supplier<ViewRenderer> renderer;

        private Singleton() {
            this.renderer = load();
        }

    }

    public static ViewRenderer get() {
        return Singleton.INSTANCE.renderer.get();
    }

    private static Supplier<ViewRenderer> load() {
        List<ViewRenderer> mappers = asList(ServiceLoader.load(ViewRenderer.class));

        if (mappers.isEmpty()) {
            return () -> {
                throw new IllegalStateException("no ViewRenderer implementation found");
            };
        }

        if (mappers.size() > 1) {
            return () -> {
                throw new IllegalStateException("multiple ViewRenderer implementations found");
            };
        }

        ViewRenderer m = mappers.get(0);

        return () -> m;
    }

    private static List<ViewRenderer> asList(Iterable<ViewRenderer> mappers) {
        List<ViewRenderer> result = new ArrayList<>();
        for (ViewRenderer mapper : mappers) {
            result.add(mapper);
        }
        return result;
    }

}
