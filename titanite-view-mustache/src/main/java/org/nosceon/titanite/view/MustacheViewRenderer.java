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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.nosceon.titanite.BodyWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.nosceon.titanite.Utils.callUnchecked;

/**
 * @author Johan Siebens
 */
public final class MustacheViewRenderer extends ViewRenderer {

    private static final MustacheViewRenderer INSTANCE = new MustacheViewRenderer(new DefaultMustacheFactory("templates"));

    public static BodyWriter render(Object view) {
        return INSTANCE.writer(view);
    }

    public static BodyWriter render(String view, Object model) {
        return INSTANCE.writer(view, model);
    }

    private static final String EXTENSION = ".mustache";

    private static final Object[] EMPTY_MODEL = new Object[0];

    private final MustacheFactory mustacheFactory;

    public MustacheViewRenderer(MustacheFactory mustacheFactory) {
        this.mustacheFactory = mustacheFactory;
    }

    @Override
    public BodyWriter writer(String template, Object view) {
        return callUnchecked(() -> render(getMustache(template), view));
    }

    private BodyWriter render(Mustache mustache, Object model) throws IOException {
        return (out) -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
                mustache.execute(writer, model instanceof String ? EMPTY_MODEL : new Object[]{model});
            }
        };
    }

    private Mustache getMustache(String template) throws IOException {
        return mustacheFactory.compile(sanitize(template));
    }

    private static String sanitize(String templateName) {
        return templateName.endsWith(EXTENSION) ? templateName : templateName + EXTENSION;
    }

}
