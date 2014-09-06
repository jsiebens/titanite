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
package org.nosceon.titanite.body;

import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.nosceon.titanite.MultiParams;
import org.nosceon.titanite.MultiPart;
import org.nosceon.titanite.exception.InvalidFormParamException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.nosceon.titanite.Utils.callUnchecked;

/**
 * @author Johan Siebens
 */
public final class FormParams extends MultiParams {

    private HttpPostRequestDecoder decoder;

    FormParams(HttpPostRequestDecoder decoder) {
        this.decoder = decoder;
    }

    public MultiPart getMultiPart(String name) {
        return
            ofNullable(decoder.getBodyHttpData(name))
                .filter(p -> p instanceof FileUpload)
                .map(p -> new MultiPart((FileUpload) p))
                .orElse(null);
    }

    @Override
    public String getString(String name) {
        return toString(decoder.getBodyHttpData(name));
    }

    @Override
    public List<String> getStrings(String name) {
        return
            decoder.getBodyHttpDatas(name)
                .stream()
                .map(this::toString)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> keys() {
        return
            unmodifiableSet(decoder
                .getBodyHttpDatas()
                .stream()
                .map(InterfaceHttpData::getName)
                .collect(toSet()));
    }

    @Override
    protected IllegalArgumentException translate(Exception e, String type, String name, String value) {
        return new InvalidFormParamException(e, type, name, value);
    }

    private String toString(InterfaceHttpData p) {
        return callUnchecked(() -> {
            if (p != null) {
                if (p instanceof FileUpload) {
                    return FileUpload.class.cast(p).getFilename();
                }
                if (p instanceof Attribute) {
                    return Attribute.class.cast(p).getValue();
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        });
    }

}
