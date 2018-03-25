package com.apache.a4javadoc.javaagent.mapper;

import java.io.Writer;

import org.springframework.stereotype.Service;

import com.apache.a4javadoc.javaagent.exception.AppRuntimeException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Proxy for {@link ObjectMapper}. Contains a single instance of {@link #objectMapper}.
 * @author Kyrylo Semenko
 */
@Service
public class ObjectMapperA4j {
    
    /** The single instance of {@link ObjectMapper} */
    private ObjectMapper objectMapper = new ObjectMapper();
    
    /** Calls a {@link ObjectMapper#writeValue(Writer, Object)} method */
    public void writeValue(Writer writer, Object value) {
        if (value == null) {
            return;
        }
        try {
            objectMapper.writeValue(writer, value);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

}
