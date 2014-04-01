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

import java.util.function.Predicate;

/**
 * @author Johan Siebens
 */
public final class MediaType implements Predicate<MediaType> {

    public final static MediaType APPLICATION_XML = valueOf("application/xml");

    public final static MediaType APPLICATION_ATOM_XML = valueOf("application/atom+xml");

    public final static MediaType APPLICATION_XHTML_XML = valueOf("application/xhtml+xml");

    public final static MediaType APPLICATION_SVG_XML = valueOf("application/svg+xml");

    public final static MediaType APPLICATION_JSON = valueOf("application/json");

    public final static MediaType APPLICATION_FORM_URLENCODED = valueOf("application/x-www-form-urlencoded");

    public final static MediaType MULTIPART_FORM_DATA = valueOf("multipart/form-data");

    public final static MediaType APPLICATION_OCTET_STREAM = valueOf("application/octet-stream");

    public final static MediaType TEXT_PLAIN = valueOf("text/plain");

    public final static MediaType TEXT_XML = valueOf("text/xml");

    public final static MediaType TEXT_HTML = valueOf("text/html");

    private final com.google.common.net.MediaType delegate;

    public static MediaType valueOf(String value) {
        return new MediaType(com.google.common.net.MediaType.parse(value));
    }

    private MediaType(com.google.common.net.MediaType delegate) {
        this.delegate = delegate;
    }

    public String type() {
        return delegate.type();
    }

    public String subtype() {
        return delegate.subtype();
    }

    public boolean is(MediaType mediaType) {
        return delegate.is(mediaType.delegate);
    }

    @Override
    public boolean test(MediaType mediaType) {
        return delegate.is(mediaType.delegate);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
