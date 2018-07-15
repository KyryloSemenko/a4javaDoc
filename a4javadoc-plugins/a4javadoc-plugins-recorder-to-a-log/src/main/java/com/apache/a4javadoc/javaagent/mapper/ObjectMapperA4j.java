package com.apache.a4javadoc.javaagent.mapper;

import java.io.Writer;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

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
        objectMapper.disable(DeserializationFeature.EAGER_DESERIALIZER_FETCH);
        objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        
        GenericSerializerProvider genericSerializerProvider = new GenericSerializerProvider();
        objectMapper.setSerializerProvider(genericSerializerProvider);
        
        SimpleModule moduleSerializer = new SimpleModule();
        moduleSerializer.addSerializer(Object.class, new GenericSerializer());
        objectMapper.registerModule(moduleSerializer);
        
        SimpleModule moduleDeserializer = new SimpleModule();
        moduleDeserializer.addDeserializer(Object.class, new GenericDeserializer());
        objectMapper.registerModule(moduleDeserializer);
    }
    
    /**
     * A faktory of the singleton instance of the {@link ObjectMapperA4j}
     * @return the singleton instance
     */
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
        try {
            objectMapper.writeValue(writer, value);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }
    
    /**
     * Calls a {@link ObjectMapper#readValue(String, Class)} method
     * @param content see a {@link ObjectMapper#readValue(String, Class)} method
     * @param valueType see a {@link ObjectMapper#readValue(String, Class)} method
     * @return a deserialized object
     */
    public <T> T readValue(String content, Class<T> valueType) {
        try {
            return (T) objectMapper.readValue(content, valueType);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }
    
    /**
     * Calls a {@link ObjectMapper#readValue(String, Class)} method with {@link Object#getClass()}
     * @param content see a {@link ObjectMapper#readValue(String, Class)} method
     * @return a deserialized object
     */
    public Object readValue(String content) {
        try {
            return objectMapper.readValue(content, Object.class);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

}
