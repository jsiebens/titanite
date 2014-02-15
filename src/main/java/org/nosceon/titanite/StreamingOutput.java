package org.nosceon.titanite;

import java.io.OutputStream;

/**
 * @author Johan Siebens
 */
public interface StreamingOutput {

    void apply(OutputStream out) throws Exception;

}
