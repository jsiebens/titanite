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
package org.nosceon.titanite.service;

import org.eclipse.jetty.util.resource.Resource;
import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;
import org.nosceon.titanite.Responses;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static java.util.Optional.ofNullable;
import static org.eclipse.jetty.util.resource.Resource.newClassPathResource;
import static org.nosceon.titanite.HttpServerException.propagate;
import static org.nosceon.titanite.Responses.*;

/**
 * @author Johan Siebens
 */
public final class ResourceService implements Function<Request, Response> {

    private String baseResource;

    public ResourceService(String baseResource) {
        this.baseResource = baseResource;
    }

    @Override
    public Response apply(Request request) {
        if (request.path.contains("..")) {
            return forbidden();
        }

        return
            ofNullable(newClassPathResource(baseResource + request.path))
                .filter((r) -> r.exists() && !r.isDirectory())
                .map((r) -> createResponse(request, r))
                .orElseGet(Responses::notFound);
    }

    private Response createResponse(Request request, Resource resource) {
        Optional<Date> ifModifiedSince = ofNullable(request.headers.getDate(IF_MODIFIED_SINCE));
        long lastModified = resource.lastModified();

        if (lastModified <= 0) {
            return
                ok()
                    .type(MimeTypes.contentType(resource.getName()))
                    .stream(propagate(resource::getInputStream));
        }
        else {
            return
                ifModifiedSince
                    .filter((d) -> lastModified <= d.getTime())
                    .map((d) -> notModified())
                    .orElseGet(() ->
                        ok()
                            .type(MimeTypes.contentType(resource.getName()))
                            .lastModified(new Date(lastModified))
                            .stream(propagate(resource::getInputStream)));
        }
    }

}
