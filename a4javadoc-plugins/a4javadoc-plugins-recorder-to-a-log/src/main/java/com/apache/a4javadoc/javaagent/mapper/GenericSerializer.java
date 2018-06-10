package com.apache.a4javadoc.javaagent.mapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * The class serializes object graph to JSON. It prevents circular dependencies.<br>
 * It uses {@link GenericSerializerProvider}.<br>
 * Each object in JSON will have a property with key {@link #GENERIC_KEY_ID} and value {@link #generateIdentifier(Object)}.
 * @author Kyrylo Semenko
 */
@SuppressWarnings("serial")
public class GenericSerializer extends StdSerializer<Object> {
    
    public static final String PRIMITIVE_OR_WRAPPER_VALUE = "value";

    /** Separator of class name and hash in for example <pre>com.apache.a4javadoc.javaagent.mapper.CircularClass@2d332eab</pre> */
    public static final String GENERIC_VALUE_SEPARATOR = "@";
    
    /** Each serialized object will have this key with value {@link #generateIdentifier(Object)} */
    public static final String GENERIC_KEY_ID = "_a4id";

    /**
     * Constructs a new object
     */
    public GenericSerializer() {
        this(null);
    }
    
    /**
     * Constructs a new object by calling the super constructor
     * @param t the object passed to the super constructor
     */
    public GenericSerializer(Class<Object> t) {
        super(t);
    }
 
    @Override
    public void serialize(Object sourceObject, JsonGenerator jsonGenerator, SerializerProvider provider) 
      throws IOException {
        if (sourceObject == null) {
            jsonGenerator.writeNull();
            return;
        }
        GenericSerializerProvider genericSerializerProvider = (GenericSerializerProvider) provider;
  
        jsonGenerator.writeStartObject();
        String identifier = generateIdentifier(sourceObject);
        jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
        if (ClassUtils.isPrimitiveOrWrapper(sourceObject.getClass())) {
            jsonGenerator.writeStringField(PRIMITIVE_OR_WRAPPER_VALUE, sourceObject.toString());
            jsonGenerator.writeEndObject();
            return;
        }
        if (genericSerializerProvider.getSerializedObjects().contains(sourceObject)) {
            jsonGenerator.writeEndObject();
            return;
        }
        genericSerializerProvider.getSerializedObjects().add(sourceObject);
        
        serialize(sourceObject, jsonGenerator, genericSerializerProvider, 1);
        
        jsonGenerator.writeEndObject();
    }

    /**
     * This method is recursive. It takes fields from sourceObject and serializes them to jsonGenerator.
     * @param sourceObject the object graph
     * @param jsonGenerator json holder
     * @param genericSerializerProvider contains serialization state
     * @param depth depth of dive where 1 is the first level from the root object
     */
    private void serialize(Object sourceObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth) {
        for (Field field : getFields(sourceObject)) {
            serializeField(sourceObject, jsonGenerator, genericSerializerProvider, depth, field);
        }
    }

    /**
     * Serialize one field of the sourceObject
     * @param sourceObject the parent of the field
     * @param jsonGenerator json target
     * @param genericSerializerProvider the serialization source
     * @param depth current depth in the root object graph
     * @param field the json source
     */
    private void serializeField(Object sourceObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Field field) {
        try {
            Object fieldObject = getObject(field, sourceObject);
            if (fieldObject == null) {
                jsonGenerator.writeNullField(field.getName());
                return;
            }
            String identifier = generateIdentifier(fieldObject);
            if (ClassUtils.isPrimitiveOrWrapper(field.getType())) {
                jsonGenerator.writeFieldName(field.getName());
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                jsonGenerator.writeStringField(PRIMITIVE_OR_WRAPPER_VALUE, fieldObject.toString());
                jsonGenerator.writeEndObject();
                return;
            }
            jsonGenerator.writeFieldName(field.getName());
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
            if (genericSerializerProvider.getSerializedObjects().contains(fieldObject)) {
                jsonGenerator.writeEndObject();
                return;
            }
            genericSerializerProvider.getSerializedObjects().add(fieldObject);
            if (depth < genericSerializerProvider.getMaxDepth()) {
                serialize(fieldObject, jsonGenerator, genericSerializerProvider, depth + 1);
            }
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new AppRuntimeException("Cannot serialize field. Field.name: " + field.getName() + ", sourceObject: " + sourceObject, e);
        }
    }

    /** @return fields from this sourceObjecta and all its parents recursively */
    private List<Field> getFields(Object sourceObject) {
        List<Field> result = new ArrayList<>();
        for (Class<?> c = sourceObject.getClass(); c != null; c = c.getSuperclass())
        {
            Field[] fields = c.getDeclaredFields();
            for (Field classField : fields)
            {
                result.add(classField);
            }
        }
        return result;
    }

    /** Try to obtain field value from getter. If getter not found, set the field accessible and obtain its value from reference. */
    private Object getObject(Field field, Object sourceObject) {
        for (Method method : sourceObject.getClass().getMethods()) {
            if (method.getName().startsWith("get")
                    && method.getName().length() == (field.getName().length() + 3)
                    && method.getName().toLowerCase().endsWith(field.getName().toLowerCase())) {
                try {
                    return method.invoke(sourceObject);
                } catch (Exception e) {
                    break;
                }
            }
        }
        field.setAccessible(true);
        try {
            return field.get(sourceObject);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * Generate an identifier from {@link Class#getName()} plus {@link #GENERIC_VALUE_SEPARATOR} plus {@link Object#hashCode()} in hexadecimal form.
     */
    String generateIdentifier(Object value) {
        return value.getClass().getName() + GENERIC_VALUE_SEPARATOR + Integer.toHexString(value.hashCode());
    }

}
