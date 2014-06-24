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
package org.nosceon.titanite;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import org.nosceon.titanite.exception.InvalidHeaderParamException;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Johan Siebens
 */
public final class HeaderParams extends MultiParams {

    private HttpMessage message;

    HeaderParams(HttpMessage message) {
        this.message = message;
    }

    @Override
    public String getString(String name) {
        return HttpHeaders.getHeader(message, name);
    }

    @Override
    public List<String> getStrings(String name) {
        return message.headers().getAll(name);
    }

    public Date getDate(String name) {
        return Optional.ofNullable(HttpHeaders.getHeader(message, name))
            .flatMap((o) -> {
                try {
                    return Optional.of(HttpHeaders.getDateHeader(message, name));
                }
                catch (ParseException e) {
                    return Optional.empty();
                }
            }).orElse(null);
    }

    @Override
    protected IllegalArgumentException translate(Exception e, String type, String name, String value) {
        return new InvalidHeaderParamException(e, type, name, value);
    }

}
