/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nosceon.titanite;

import java.util.concurrent.Callable;

import static org.nosceon.titanite.Exceptions.internalServerError;

/**
 * @author Johan Siebens
 */
public final class HttpServerException extends RuntimeException {

    private Response response;

    public HttpServerException(String message, Response response) {
        super(message);
        this.response = response;
    }

    public HttpServerException(Throwable cause, Response response) {
        super(String.valueOf(response.status()), cause);
        this.response = response;
    }

    public static <T> T call(Callable<T> callable) throws HttpServerException {
        try {
            return callable.call();
        }
        catch (HttpServerException e1) {
            throw e1;
        }
        catch (Exception e11) {
            throw new HttpServerException(e11, internalServerError());
        }
    }

    public static void run(Action runnable) throws HttpServerException {
        try {
            runnable.run();
        }
        catch (HttpServerException e1) {
            throw e1;
        }
        catch (Exception e11) {
            throw new HttpServerException(e11, internalServerError());
        }
    }

    public Response getResponse() {
        return response;
    }

    @FunctionalInterface
    public static interface Action {

        void run() throws Exception;

    }

}
