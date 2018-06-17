package com.apache.a4javadoc.javaagent.mapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(GenericSerializer.class);
    
    private static final String GETTER_PREFIX = "get";

    /** The key of a primitive or wrapper value */
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
        serializeObject(sourceObject, jsonGenerator, (GenericSerializerProvider) provider, 1, sourceObject, true);
    }

    /**  */
    private void serializeObject(Object sourceObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, boolean appendGenericId) {
        try {
            if (sourceObject == null) {
                jsonGenerator.writeNull();
                return;
            }
      
            String identifier = generateIdentifier(sourceObject);
        
            if (processPrimitiveOrWrapperOrString(jsonGenerator, sourceObject, identifier, appendGenericId)) {
                return;
            }
            if (processArrayOrList(sourceObject, jsonGenerator, genericSerializerProvider, depth, rootObject)) {
                return;
            }
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
            genericSerializerProvider.getSerializedObjects().add(sourceObject);
            
            serializeSourceObjectFields(sourceObject, jsonGenerator, genericSerializerProvider, depth, rootObject);
            
            jsonGenerator.writeEndObject();
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * This method is recursive. It takes fields from sourceObject and serializes them to jsonGenerator.
     * @param sourceObject the object graph
     * @param jsonGenerator json holder
     * @param genericSerializerProvider contains serialization state
     * @param depth depth of dive where 1 is the first level from the root object
     * @param rootObject top object in object graph
     */
    private void serializeSourceObjectFields(Object sourceObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject) {
        for (Field field : getFields(sourceObject)) {
            serializeField(sourceObject, jsonGenerator, genericSerializerProvider, depth, field, rootObject);
        }
    }

    /**
     * Serialize one field of the sourceObject
     * @param sourceObject the parent of the field
     * @param jsonGenerator json target
     * @param genericSerializerProvider the serialization source
     * @param depth current depth in the root object graph
     * @param field the json source
     * @param rootObject top object in object graph
     */
    private void serializeField(Object sourceObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Field field, Object rootObject) {
        try {
            Object fieldObject = getObject(field, sourceObject);
            if (fieldObject == null) {
                jsonGenerator.writeNullField(field.getName());
                return;
            }
            String identifier = generateIdentifier(fieldObject);
            if (genericSerializerProvider.getSerializedObjects().contains(fieldObject)) {
                jsonGenerator.writeFieldName(field.getName());
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                jsonGenerator.writeEndObject();
                return;
            }
            genericSerializerProvider.getSerializedObjects().add(fieldObject);
            jsonGenerator.writeFieldName(field.getName());
            
            // toto asi sem jiz nepatri
//            if (processArrayOrList(fieldObject, jsonGenerator, genericSerializerProvider, depth, rootObject)) {
//                return;
//            }
//            jsonGenerator.writeStartObject();
//            jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
//            if (processPrimitiveOrWrapper(jsonGenerator, fieldObject)) {
//                return;
//            }
//            if (depth < genericSerializerProvider.getMaxDepth()) {
//                serializeSourceObjectFields(fieldObject, jsonGenerator, genericSerializerProvider, depth + 1, rootObject);
//            } else {
//                logger.info("Maximum depth {} is reached, this object will not be serialized deeper. Root object: '{}'",
//                        depth, rootObject.getClass().getCanonicalName());
//            }
//            jsonGenerator.writeEndObject();
            serializeObject(fieldObject, jsonGenerator, genericSerializerProvider, depth, rootObject, false);
        } catch (IOException e) {
            throw new AppRuntimeException("Cannot serialize field. Field.name: " + field.getName() + ", sourceObject: " + sourceObject, e);
        }
    }

    /**  */
    private boolean processArrayOrList(Object fieldObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject) {
        try {
            if (fieldObject instanceof Iterable) {
                jsonGenerator.writeStartArray();
                
                List<Object> objectList = new ArrayList<>();
                Set<Class<?>> classSet = new HashSet<>();
                @SuppressWarnings("unchecked")
                Iterator<Object> iterator = ((Iterable<Object>) fieldObject).iterator();
                while(iterator.hasNext()) {
                    Object nextObject = iterator.next();
                    objectList.add(nextObject);
                    classSet.add(nextObject.getClass());
                }
                for (Object nextObject : objectList) {
                    serializeObject(nextObject, jsonGenerator, genericSerializerProvider, depth, rootObject, classSet.size() > 1);
                }
                
                jsonGenerator.writeEndArray();
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new AppRuntimeException(e);
        }
            
    }

    /**
     * If the object is primitive or wrapper or String, add its value to jsonGenerator and return 'true',
     * else do nothing and return 'false';
     * @param appendGenericId should be {@link #GENERIC_KEY_ID} and {@link #GENERIC_VALUE_SEPARATOR} inserted to JSON?
     */
    private boolean processPrimitiveOrWrapperOrString(JsonGenerator jsonGenerator, Object object, String identifier, boolean appendGenericId) throws IOException {
        if (ClassUtils.isPrimitiveOrWrapper(object.getClass()) || object instanceof String) {
            if (appendGenericId) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                jsonGenerator.writeStringField(PRIMITIVE_OR_WRAPPER_VALUE, object.toString());
                jsonGenerator.writeEndObject();
            } else {
                jsonGenerator.writeString(object.toString());
            }
            return true;
        }
        return false;
    }

    /** @return fields from this sourceObjecta and all its parents recursively */
    private List<Field> getFields(Object sourceObject) {
        List<Field> result = new ArrayList<>();
        for (Class<?> c = sourceObject.getClass(); c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field classField : fields) {
                if (isValidField(classField)) {
                    result.add(classField);
                }
            }
        }
        return result;
    }

    /**
     * @return false if the field is synthetic or static
     */
    private boolean isValidField(Field field) {
        return !field.isSynthetic() && !Modifier.isStatic(field.getModifiers());
    }

    /** Try to obtain field value from getter. If getter not found, set the field accessible and obtain its value from reference. */
    private Object getObject(Field field, Object sourceObject) {
        for (Method method : sourceObject.getClass().getMethods()) {
            if (method.getParameterTypes().length == 0
                    && method.getName().startsWith(GETTER_PREFIX)
                    && method.getName().length() == (field.getName().length() + GETTER_PREFIX.length())
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
