package com.apache.a4javadoc.javaagent.mapper;

import java.io.Writer;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Proxy for {@link ObjectMapper}. Contains a single instance of {@link #objectMapper}.
 * @author Kyrylo Semenko
 */
public class ObjectMapperA4j {
    
    /** The single instance of {@link ObjectMapper} */
    private ObjectMapper objectMapper;
    
    private static ObjectMapperA4j instance;
    
    /** Create an instance of the {@link #objectMapper} and configure it */
    private ObjectMapperA4j() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
    
    /** A faktory of the singleton instance of the {@link ObjectMapperA4j} */
    public static ObjectMapperA4j getInstance() {
        if (instance == null) {
            instance = new ObjectMapperA4j();
        }
        return instance;
    }
    
    /**
     * Calls a {@link ObjectMapper#writeValue(Writer, Object)} method
     * @param writer see a {@link ObjectMapper#writeValue(Writer, Object)} method
     * @param value see a {@link ObjectMapper#writeValue(Writer, Object)} method
     */
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
