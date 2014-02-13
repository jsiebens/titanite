package org.nosceon.titanite;

import java.io.InputStream;

/**
 * @author Johan Siebens
 */
public interface RequestBody {

    InputStream asStream();

    boolean isForm();

    FormParams asForm();

}
