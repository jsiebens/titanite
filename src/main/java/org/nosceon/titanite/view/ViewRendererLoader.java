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
package org.nosceon.titanite.view;

import org.nosceon.titanite.view.impl.FreemarkerViewRenderer;
import org.nosceon.titanite.view.impl.MustacheViewRenderer;

import java.util.Optional;

/**
 * @author Johan Siebens
 */
public enum ViewRendererLoader {

    INSTANCE;

    private final Optional<ViewRenderer> renderer;

    private ViewRendererLoader() {
        this.renderer = Optional.ofNullable(load());
    }

    public static ViewRenderer get() {
        return INSTANCE.renderer.get();
    }

    private static ViewRenderer load() {
        if (classIsAvailable("com.github.mustachejava.Mustache")) {
            return new MustacheViewRenderer();
        }

        if (classIsAvailable("freemarker.template.Configuration")) {
            return new FreemarkerViewRenderer();
        }

        return null;
    }

    private static boolean classIsAvailable(String name) {
        try {
            Class.forName(name);
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

}
