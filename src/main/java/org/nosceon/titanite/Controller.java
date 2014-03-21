package org.nosceon.titanite;

import java.util.concurrent.CompletableFuture;

/**
 * @author Johan Siebens
 */
public abstract class Controller extends Routings<Request, CompletableFuture<Response>> {

}
