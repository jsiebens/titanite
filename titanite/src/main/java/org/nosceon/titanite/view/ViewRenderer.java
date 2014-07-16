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

import org.nosceon.titanite.BodyWriter;

/**
 * @author Johan Siebens
 */
public abstract class ViewRenderer {

    public final BodyWriter apply(Object modelAndView) {
        return apply(templateOf(modelAndView), modelAndView);
    }

    public abstract BodyWriter apply(String view, Object model);

    private String templateOf(Object o) {
        if (o instanceof String) {
            return (String) o;
        }

        if (o instanceof View) {
            return ((View) o).template;
        }

        View.Template template = o.getClass().getAnnotation(View.Template.class);

        if (template != null) {
            return template.value();
        }

        throw new IllegalArgumentException(
            "instance of " + o.getClass() + " does not match [" + String.class.getName() + "] or [" + View.class.getName() + "], or is not annotated with [" + View.Template.class.getName() + "]"
        );
    }

}
