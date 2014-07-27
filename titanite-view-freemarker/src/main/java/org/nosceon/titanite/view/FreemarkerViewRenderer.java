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

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.nosceon.titanite.BodyWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;

import static org.nosceon.titanite.Utils.callUnchecked;

/**
 * @author Johan Siebens
 */
public final class FreemarkerViewRenderer extends ViewRenderer {

    private static FreemarkerViewRenderer INSTANCE = new FreemarkerViewRenderer(defaultConfiguration());

    public static BodyWriter render(Object view) {
        return INSTANCE.writer(view);
    }

    public static BodyWriter render(String view, Object model) {
        return INSTANCE.writer(view, model);
    }

    private static final String EXTENSION = ".ftl";

    private final Configuration configuration;

    public FreemarkerViewRenderer(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public BodyWriter writer(String view, Object model) {
        return callUnchecked(() -> render(getTemplate(view), model));
    }

    private BodyWriter render(Template template, Object model) throws IOException {
        return (out) -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
                template.process(model instanceof String ? Collections.EMPTY_MAP : model, writer);
            }
        };
    }

    private Template getTemplate(String template) throws IOException {
        return configuration.getTemplate(sanitize(template));
    }

    private static String sanitize(String templateName) {
        return templateName.endsWith(EXTENSION) ? templateName : templateName + EXTENSION;
    }

    private static Configuration defaultConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(FreemarkerViewRenderer.class, "/templates");
        return configuration;
    }

}
