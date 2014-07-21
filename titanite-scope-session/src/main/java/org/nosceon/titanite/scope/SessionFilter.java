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
package org.nosceon.titanite.scope;

import org.nosceon.titanite.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.HttpServerException.call;
import static org.nosceon.titanite.Utils.checkNotEmpty;
import static org.nosceon.titanite.Utils.deserialize;

/**
 * @author Johan Siebens
 */
public final class SessionFilter implements Filter {

    public static final String DEFAULT_SESSION_COOKIE_NAME = "_session";

    private static final String UTF_8 = "UTF-8";

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private final String cookieName;

    private final String secret;

    public SessionFilter(String secret) {
        this(DEFAULT_SESSION_COOKIE_NAME, secret);
    }

    public SessionFilter(String cookieName, String secret) {
        this.cookieName = checkNotEmpty(cookieName, "cookieName is required");
        this.secret = checkNotEmpty(secret, "secret is required");
    }

    @Override
    public CompletionStage<Response> apply(Request request, Function<Request, CompletionStage<Response>> function) {
        Session scope =
            ofNullable(request.cookies().getString(cookieName))
                .map(this::decode)
                .map(Session::new)
                .orElseGet(Session::new);

        return function.apply(request.withAttribute(Session.ATTRIBUTE_ID, scope)).thenApply(
            resp -> resp.cookie(encode(scope.values()))
        );
    }

    Cookie encode(Map<String, String> values) {
        String serialized = Utils.serialize(values);
        String signed = sign(secret, serialized) + '|' + serialized;
        return new Cookie(cookieName, signed).httpOnly(true).path("/");
    }

    private Map<String, String> decode(String value) {
        int i = value.indexOf('|');

        if (i == -1) {
            return Collections.emptyMap();
        }

        String signature = value.substring(0, i);
        String values = value.substring(i + 1);

        return safeEquals(signature, sign(secret, values)) ? deserialize(values) : Collections.emptyMap();
    }

    private static String sign(String secret, String message) {
        return
            call(() -> {
                Key secretKey = new SecretKeySpec(secret.getBytes(UTF_8), HMAC_SHA1_ALGORITHM);
                Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
                mac.init(secretKey);
                byte[] bytes = mac.doFinal(message.getBytes(UTF_8));
                return encodeHexString(bytes);
            });
    }

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String encodeHexString(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }
        return new String(out);
    }

    private static boolean safeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        else {
            int equal = 0;
            for (int i = 0; i < a.length(); i++) {
                equal |= a.charAt(i) ^ b.charAt(i);
            }
            return equal == 0;
        }
    }

}
