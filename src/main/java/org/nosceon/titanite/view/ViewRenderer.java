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

import org.nosceon.titanite.Request;

import java.io.OutputStream;

/**
 * @author Johan Siebens
 */
public interface ViewRenderer {

    boolean isTemplateAvailable(Object view);

    void render(Request request, Object view, OutputStream out) throws Exception;

    public default String templateOf(Object o) {
        if (o instanceof View) {
            return ((View) o).template;
        }
        else {
            ViewTemplate template = o.getClass().getAnnotation(ViewTemplate.class);
            if (template != null) {
                return template.value();
            }

            throw new IllegalArgumentException(o.getClass() + " does not extend View or is not annotated with ViewTemplate");
        }
    }

}
