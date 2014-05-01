package org.nosceon.titanite.service;

import java.util.concurrent.Executor;

/**
 * @author Johan Siebens
 */
public final class PublicResourceService extends ResourceService {

    private static final String PATH = "/public";

    public PublicResourceService() {
        super(PATH);
    }

    public PublicResourceService(Executor executor) {
        super(PATH, executor);
    }

}
