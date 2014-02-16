package org.nosceon.titanite.service;

import com.google.common.io.Resources;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;

/**
 * @author Johan Siebens
 */
class MimeTypes {

    private static MimetypesFileTypeMap map;

    static {
        try {
            map = new MimetypesFileTypeMap(Resources.getResource("META-INF/mime.types").openStream());
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String contentType(String name) {
        return map.getContentType(name);
    }

}
