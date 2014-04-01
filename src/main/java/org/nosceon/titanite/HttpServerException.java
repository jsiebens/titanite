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
import java.util.function.Function;

/**
 * @author Johan Siebens
 */
public final class HttpServerException extends RuntimeException {

    private Response response;

    public HttpServerException(Response response) {
        this.response = response;
    }

    public HttpServerException(Throwable cause, Response response) {
        super(cause);
        this.response = response;
    }

    public static <T> T propagate(Callable<T> callable) throws HttpServerException {
        return propagate(callable, e -> Responses.internalServerError());
    }

    public static <T> T propagate(Callable<T> callable, Function<Exception, Response> translator) throws HttpServerException {
        try {
            return callable.call();
        }
        catch (HttpServerException e) {
            throw e;
        }
        catch (Exception e) {
            throw new HttpServerException(e, translator.apply(e));
        }
    }

    public static <R, S> Function<R, S> wrap(Function<R, S> f) throws HttpServerException {
        return wrap(f, e -> Responses.internalServerError());
    }

    public static <R, S> Function<R, S> wrap(Function<R, S> f, Function<Exception, Response> translator) throws HttpServerException {
        return s -> {
            try {
                return f.apply(s);
            }
            catch (HttpServerException e) {
                throw e;
            }
            catch (Exception e) {
                throw new HttpServerException(e, translator.apply(e));
            }
        };
    }

    public Response getResponse() {
        return response;
    }

}
