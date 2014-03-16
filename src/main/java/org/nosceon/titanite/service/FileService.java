package org.nosceon.titanite.service;

import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;
import org.nosceon.titanite.Responses;

import java.io.File;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.Responses.notModified;
import static org.nosceon.titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public final class FileService implements Function<Request, Response> {

    private final File docRoot;

    public FileService(File docRoot) {
        this.docRoot = docRoot;
    }

    @Override
    public Response apply(Request request) {
        return
            ofNullable(new File(docRoot, request.path))
                .filter((r) -> r.exists() && r.canRead() && !r.isDirectory())
                .map((r) -> createResponse(request, r))
                .orElseGet(Responses::notFound);
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
