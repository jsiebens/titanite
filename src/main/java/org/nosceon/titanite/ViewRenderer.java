package org.nosceon.titanite;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.netty.handler.codec.http.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Johan Siebens
 */
public final class ViewRenderer {

    private static final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    private static final String EXTENSION = ".mustache";

    public boolean isTemplateAvailable(View view) {
        try {
            return getMustache(view.template) != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void render(HttpRequest request, View view, OutputStream out) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
            Mustache mustache = getMustache(view.template);
            mustache.execute(writer, new Object[]{view});
        }
    }

    private Mustache getMustache(String key) throws IOException {
        return mustacheFactory.compile("templates" + sanitize(key));
    }

    private static String sanitize(String templateName) {
        String s = templateName;
        if (!s.endsWith(EXTENSION)) {
            s = s + EXTENSION;
        }

        if (!s.startsWith("/")) {
            s = "/" + s;
        }

        return s;
    }

}
