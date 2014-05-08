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
package org.nosceon.titanite.view.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.nosceon.titanite.Request;
import org.nosceon.titanite.Titanite;
import org.nosceon.titanite.view.ViewRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Johan Siebens
 */
public class FreemarkerViewRenderer implements ViewRenderer {

    private static final String EXTENSION = ".ftl";

    private final Configuration configuration = defaultConfiguration();

    @Override
    public boolean isTemplateAvailable(Object view) {
        try {
            return getTemplate(view) != null;
        }
        catch (IOException e) {
            return false;
        }
    }

    @Override
    public void render(Request request, Object view, OutputStream out) throws Exception {
        try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
            getTemplate(view).process(view, writer);
        }
    }

    private Template getTemplate(Object view) throws IOException {
        return configuration.getTemplate(sanitize(templateOf(view)));
    }

    private static String sanitize(String templateName) {
        return templateName.endsWith(EXTENSION) ? templateName : templateName + EXTENSION;
    }

    private static Configuration defaultConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(Titanite.class, "/templates");
        return configuration;
    }

}
