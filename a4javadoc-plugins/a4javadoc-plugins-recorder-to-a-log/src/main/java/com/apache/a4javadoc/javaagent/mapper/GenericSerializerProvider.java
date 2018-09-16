package com.apache.a4javadoc.javaagent.mapper;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;

/**
 * <p>
 * Implementation of {@link DefaultSerializerProvider}.
 * Contains serialization state, for example {@link #serializedObjects}
 * or {@link #maxDepth}.
 * <p>
 * Instance of this object creates before serialization and disappears
 * after serialization.
 * @author Kyrylo Semenko
 */
@SuppressWarnings("serial")
public class GenericSerializerProvider extends DefaultSerializerProvider {
    /**
     * Used for preventing of circular dependencies.<br>
     * If this set contains some object, then the object will not been serialized again.<br>
     * Its unique identifier will be serialized instead.
     */
    private transient Set<Object> serializedObjects;
    
    /** How levels of object graph should be serialized */
    private int maxDepth;
    
    /**
     * Default constructor.
     */
    public GenericSerializerProvider() {
        super();
        serializedObjects = new HashSet<>();
    }
    
    /**
     * Constructor with parameter.
     * @param genericSerializerProvider
     */
    public GenericSerializerProvider(GenericSerializerProvider genericSerializerProvider) {
        super(genericSerializerProvider);
        serializedObjects = new HashSet<>();
    }
    
    protected GenericSerializerProvider(SerializerProvider serializerProvider, SerializationConfig serializationConfig,
            SerializerFactory serializerFactory) {
        super(serializerProvider, serializationConfig, serializerFactory);
        serializedObjects = new HashSet<>();
    }

    @Override
    public DefaultSerializerProvider copy()
    {
        if (getClass() != GenericSerializerProvider.class) {
            return super.copy();
        }
        return new GenericSerializerProvider(this);
    }

    /** 
     * @see com.fasterxml.jackson.databind.ser.DefaultSerializerProvider#createInstance(com.fasterxml.jackson.databind.SerializationConfig, com.fasterxml.jackson.databind.ser.SerializerFactory)
     */
    @Override
    public DefaultSerializerProvider createInstance(SerializationConfig serializationConfig, SerializerFactory serializerFactory) {
        return new GenericSerializerProvider(this, serializationConfig, serializerFactory);
    }

    /** @return The {@link GenericSerializerProvider#serializedObjects} field */
    public Set<Object> getSerializedObjects() {
        return serializedObjects;
    }

    /** @param serializedObjects see the {@link GenericSerializerProvider#serializedObjects} field */
    public void setSerializedObjects(Set<Object> serializedObjects) {
        this.serializedObjects = serializedObjects;
    }

    /** @return The {@link GenericSerializerProvider#maxDepth} field */
    public int getMaxDepth() {
        return maxDepth;
    }

    /** @param maxDepth see the {@link GenericSerializerProvider#maxDepth} field */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

}
