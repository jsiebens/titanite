package org.nosceon.titanite.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nosceon.titanite.BodyReader;
import org.nosceon.titanite.BodyWriter;

/**
 * @author Johan Siebens
 */
public final class JacksonMapper {

    private static JacksonMapper INSTANCE = new JacksonMapper(new ObjectMapper());

    public static <T> BodyReader<T> json(Class<T> type) {
        return INSTANCE.reader(type);
    }

    public static BodyWriter json(Object value) {
        return INSTANCE.writer(value);
    }

    private final ObjectMapper mapper;

    public JacksonMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    private <T> BodyReader<T> reader(Class<T> type) {
        return in -> mapper.readValue(in, type);
    }

    private BodyWriter writer(Object value) {
        return (out) -> mapper.writeValue(out, value);
    }

}
