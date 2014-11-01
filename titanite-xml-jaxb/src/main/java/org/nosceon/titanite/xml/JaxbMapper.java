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
package org.nosceon.titanite.xml;

import org.nosceon.titanite.BodyReader;
import org.nosceon.titanite.BodyWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Johan Siebens
 */
public final class JaxbMapper {

    private static JaxbMapper INSTANCE = new JaxbMapper(false);

    public static <T> BodyReader<T> xml(Class<T> type) {
        return INSTANCE.reader(type);
    }

    public static BodyWriter xml(Object value) {
        return INSTANCE.writer(value);
    }

    private static final Map<Class, JAXBContext> JAXB_CONTEXTS = new WeakHashMap<>();

    private final boolean formatted;

    public JaxbMapper(boolean formatted) {
        this.formatted = formatted;
    }

    public <T> BodyReader<T> reader(Class<T> type) {
        return in -> {
            try (Reader reader = new InputStreamReader(in)) {
                return type.cast(unmarshaller(type).unmarshal(reader));
            }
        };
    }

    public BodyWriter writer(Object value) {
        return out -> {
            try (Writer w = new BufferedWriter(new OutputStreamWriter(out))) {
                marshaller(value.getClass()).marshal(value, w);
            }
        };
    }

    private Marshaller marshaller(Class<?> type) throws JAXBException {
        Marshaller marshaller = getJAXBContext(type).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
        return marshaller;
    }

    private Unmarshaller unmarshaller(Class<?> type) throws JAXBException {
        return getJAXBContext(type).createUnmarshaller();
    }

    private static JAXBContext getJAXBContext(Class<?> type) throws JAXBException {
        synchronized (JAXB_CONTEXTS) {
            JAXBContext c = JAXB_CONTEXTS.get(type);
            if (c == null) {
                c = JAXBContext.newInstance(type);
                JAXB_CONTEXTS.put(type, c);
            }
            return c;
        }
    }

}
