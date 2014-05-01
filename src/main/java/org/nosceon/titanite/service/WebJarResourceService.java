package org.nosceon.titanite.service;

import java.util.concurrent.Executor;

/**
 * @author Johan Siebens
 */
public final class WebJarResourceService extends ResourceService {

    private static final String PATH = "/META-INF/resources/webjars";

    public WebJarResourceService() {
        super(PATH);
    }

    public WebJarResourceService(Executor executor) {
        super(PATH, executor);
    }

}
