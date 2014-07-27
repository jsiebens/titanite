/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.File;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.Utils.getMediaTypeFromFileName;

/**
 * @author Johan Siebens
 */
public final class FileService implements Function<Request, CompletionStage<Response>> {

    public static Function<Request, CompletionStage<Response>> fileService(String directory) {
        return new FileService(new File(directory));
    }

    public static Function<Request, CompletionStage<Response>> fileService(String directory, Function<Request, String> path) {
        return new FileService(new File(directory), path);
    }

    public static Function<Request, CompletionStage<Response>> fileService(File directory) {
        return new FileService(directory);
    }

    public static Function<Request, CompletionStage<Response>> fileService(File directory, Function<Request, String> path) {
        return new FileService(directory, path);
    }

    public static Response serveFile(Request request, File file) {
        return
            ofNullable(file)
                .filter((r) -> r.exists() && r.canRead() && !r.isHidden() && !r.isDirectory())
                .map((r) -> createResponse(request, r))
                .orElseGet(Response::notFound);
    }

    private final File docRoot;

    private final Function<Request, String> pathExtractor;

    public FileService(File docRoot) {
        this(docRoot, Request::path);
    }

    public FileService(File docRoot, Function<Request, String> pathExtractor) {
        this.docRoot = docRoot;
        this.pathExtractor = pathExtractor;
    }

    @Override
    public CompletionStage<Response> apply(Request request) {
        String path = Optional.ofNullable(pathExtractor.apply(request)).get();

        if (path.contains("..")) {
            return Response.forbidden().toFuture();
        }

        File file = new File(docRoot, path);

        return file.isDirectory() ? serveFile(request, new File(file, "index.html")).toFuture() : serveFile(request, file).toFuture();
    }

    private static Response createResponse(Request request, File file) {
        Optional<Date> ifModifiedSince = ofNullable(request.headers().getDate(IF_MODIFIED_SINCE));
        long lastModified = file.lastModified();

        if (lastModified <= 0) {
            return
                Response.ok()
                    .type(getMediaTypeFromFileName(file.getName()))
                    .body(file);
        }
        else {
            return
                ifModifiedSince
                    .filter((d) -> lastModified <= d.getTime())
                    .map((d) -> Response.notModified())
                    .orElseGet(() ->
                        Response.ok()
                            .type(getMediaTypeFromFileName(file.getName()))
                            .lastModified(new Date(lastModified))
                            .body(file));
        }
    }

}
