package org.nosceon.titanite;

import java.io.InputStream;

/**
 * @author Johan Siebens
 */
public interface RequestBody {

    InputStream asStream();

    <T> T asJson(Class<T> type);

    boolean isForm();

    FormParams asForm();

}
