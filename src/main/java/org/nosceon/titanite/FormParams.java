package org.nosceon.titanite;

import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import java.util.Optional;

import static java.util.Optional.*;
import static org.nosceon.titanite.HttpServerException.propagate;

/**
 * @author Johan Siebens
 */
public final class FormParams extends Params {

    private HttpPostRequestDecoder decoder;

    FormParams(HttpPostRequestDecoder decoder) {
        this.decoder = decoder;
    }

    public Optional<MultiPart> getMultiPart(String name) {
        return
            ofNullable(decoder.getBodyHttpData(name))
                .filter(p -> p instanceof FileUpload)
                .map(p -> new MultiPart((FileUpload) p));
    }

    @Override
    public Optional<String> getString(String name) {
        return
            ofNullable(decoder.getBodyHttpData(name))
                .flatMap(p ->
                    propagate(() -> {
                        if (p instanceof FileUpload) {
                            return of(FileUpload.class.cast(p).getFilename());
                        }
                        if (p instanceof Attribute) {
                            return of(Attribute.class.cast(p).getValue());
                        }
                        else {
                            return empty();
                        }
                    }));
    }

}
