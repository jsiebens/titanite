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

import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;
import org.nosceon.titanite.Titanite;

import java.io.File;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.nosceon.titanite.Titanite.Responses.*;

/**
 * @author Johan Siebens
 */
public final class FileService implements Function<Request, CompletionStage<Response>> {

    private final File docRoot;

    private final Executor executor;

    public FileService(File docRoot) {
        this(docRoot, Runnable::run);
    }

    public FileService(File docRoot, Executor executor) {
        this.docRoot = docRoot;
        this.executor = executor;
    }

    @Override
    public CompletionStage<Response> apply(Request request) {
        return supplyAsync(() -> internalApply(request), executor);
    }

    private Response internalApply(Request request) {
        if (request.path.contains("..")) {
            return forbidden();
        }

        return
            ofNullable(new File(docRoot, request.path))
                .filter((r) -> r.exists() && r.canRead() && !r.isHidden() && !r.isDirectory())
                .map((r) -> createResponse(request, r))
                .orElseGet(Titanite.Responses::notFound);
    }

    private Response createResponse(Request request, File file) {
        Optional<Date> ifModifiedSince = ofNullable(request.headers.getDate(IF_MODIFIED_SINCE));
        long lastModified = file.lastModified();

        if (lastModified <= 0) {
            return
                ok()
                    .type(MimeTypes.contentType(file.getName()))
                    .file(file);
        }
        else {
            return
                ifModifiedSince
                    .filter((d) -> lastModified <= d.getTime())
                    .map((d) -> notModified())
                    .orElseGet(() ->
                        ok()
                            .type(MimeTypes.contentType(file.getName()))
                            .lastModified(new Date(lastModified))
                            .file(file));
        }
    }

}
