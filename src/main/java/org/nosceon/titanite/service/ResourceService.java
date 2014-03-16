package org.nosceon.titanite.service;

import com.google.common.io.ByteStreams;
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
import static org.nosceon.titanite.Responses.notModified;
import static org.nosceon.titanite.Responses.ok;

/**
 * @author Johan Siebens
 */
public final class ResourceService implements Function<Request, Response> {

    public static final Function<Request, Response> WEBJAR_RESOURCES = new ResourceService("/META-INF/resources/webjars");

    public static final Function<Request, Response> PUBLIC_RESOURCES = new ResourceService("/public");

    private String baseResource;

    public ResourceService(String baseResource) {
        this.baseResource = baseResource;
    }

    @Override
    public Response apply(Request request) {
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
                    .stream((o) -> {
                        ByteStreams.copy(resource.getInputStream(), o);
                    });
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
                            .stream((o) -> {
                                ByteStreams.copy(resource.getInputStream(), o);
                            }));
        }
    }

}
