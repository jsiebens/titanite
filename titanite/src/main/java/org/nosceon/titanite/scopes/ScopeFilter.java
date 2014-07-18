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
package org.nosceon.titanite.scopes;

import org.nosceon.titanite.Cookie;
import org.nosceon.titanite.Filter;
import org.nosceon.titanite.Request;
import org.nosceon.titanite.Response;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.nosceon.titanite.HttpServerException.call;

/**
 * @author Johan Siebens
 */
abstract class ScopeFilter<S extends Scope> implements Filter {

    private static final String UTF_8 = "UTF-8";

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private final String attributeId;

    private final String cookieName;

    private final String secret;

    public ScopeFilter(String attributeId, String cookieName) {
        this(attributeId, cookieName, null);
    }

    public ScopeFilter(String attributeId, String cookieName, String secret) {
        this.attributeId = attributeId;
        this.cookieName = cookieName;
        this.secret = secret;
    }

    protected abstract S newScope();

    protected abstract S newScope(Map<String, String> values);

    @Override
    public CompletionStage<Response> apply(Request request, Function<Request, CompletionStage<Response>> function) {
        S scope =
            ofNullable(request.cookies().getString(cookieName))
                .map(this::decode)
                .map(this::newScope)
                .orElseGet(this::newScope);

        return function.apply(request.withAttribute(attributeId, scope)).thenApply(
            resp -> resp.cookie(encode(scope.values()))
        );
    }

    Cookie encode(Map<String, String> values) {
        String serialized =
            values.entrySet()
                .stream()
                .filter(e -> e.getValue() != null && e.getValue().trim().length() > 0)
                .map(e -> urlencode(e.getKey(), e.getValue()))
                .reduce("", (s, s2) -> s.length() == 0 ? s2 : s + '&' + s2);

        String value = secret == null ? serialized : (sign(secret, serialized) + '|' + serialized);

        return new Cookie(cookieName, value).httpOnly(true).path("/");
    }

    private Map<String, String> decode(String value) {
        if (secret != null) {
            int i = value.indexOf('|');

            if (i == -1) {
                return Collections.emptyMap();
            }

            String signature = value.substring(0, i);
            String values = value.substring(i + 1);

            return safeEquals(signature, sign(secret, values)) ? deserialize(values) : Collections.emptyMap();
        }
        else {
            return deserialize(value);
        }
    }

    private Map<String, String> deserialize(String values) {
        return
            ofNullable(values)
                .filter(v -> v != null && v.trim().length() > 0)
                .map(v -> {
                    Map<String, String> result = new LinkedHashMap<>();
                    String[] split = v.split("&");
                    for (String s : split) {
                        String[] keyValue = s.split("=");
                        result.put(
                            urldecode(keyValue[0]),
                            urldecode(keyValue[1])
                        );
                    }
                    return result;
                })
                .orElseGet(Collections::emptyMap);
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

    private static String urlencode(String key, String value) {
        return call(() -> URLEncoder.encode(key, UTF_8) + '=' + URLEncoder.encode(value, UTF_8));
    }

    private static String urldecode(String value) {
        return call(() -> URLDecoder.decode(value, UTF_8));
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
