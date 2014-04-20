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

import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import java.util.Optional;

import static java.util.Optional.*;
import static org.nosceon.titanite.HttpServerException.propagate;

/**
 * @author Johan Siebens
 */
public final class FormParams implements SingleParams {

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
    public String getString(String name) {
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
                    }))
                .orElse(null);
    }

}
