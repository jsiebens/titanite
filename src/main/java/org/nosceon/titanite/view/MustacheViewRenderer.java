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
import org.nosceon.titanite.view.ViewRenderer;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.nosceon.titanite.HttpServerException.call;

/**
 * @author Johan Siebens
 */
public final class MustacheViewRenderer implements ViewRenderer {

    private static final String EXTENSION = ".mustache";

    private final MustacheFactory mustacheFactory = defaultMustacheFactory();

    @Override
    public BodyWriter apply(Object view) {
        return call(() -> render(getMustache(view), view));
    }

    private BodyWriter render(Mustache mustache, Object view) throws IOException {
        return (out) -> {
            try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
                mustache.execute(writer, new Object[]{view});
            }
        };
    }

    private Mustache getMustache(Object view) throws IOException {
        return mustacheFactory.compile(sanitize(templateOf(view)));
    }

    private static String sanitize(String templateName) {
        return templateName.endsWith(EXTENSION) ? templateName : templateName + EXTENSION;
    }

    private static MustacheFactory defaultMustacheFactory() {
        return new DefaultMustacheFactory("templates");
    }

}
