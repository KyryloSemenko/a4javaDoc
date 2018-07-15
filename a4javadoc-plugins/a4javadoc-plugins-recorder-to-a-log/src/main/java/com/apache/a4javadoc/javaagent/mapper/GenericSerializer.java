package com.apache.a4javadoc.javaagent.mapper;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    public static final String GENERIC_VALUE = "value";

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
        logger.trace("The beginning of serialization. Object: '{}'", sourceObject);
        serializeObject(null, sourceObject, jsonGenerator, (GenericSerializerProvider) provider, 1, sourceObject, true);
    }

    // TODO Kyrylo Semenko
    private void serializeObject(Field field, Object sourceObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, boolean appendGenericId) {
        try {
            if (processNull(field, sourceObject, jsonGenerator)) {
                return;
            }
            
            String identifier = generateIdentifier(sourceObject);
            
//            if (field != null && genericSerializerProvider.getSerializedObjects().contains(sourceObject)) {
            if (genericSerializerProvider.getSerializedObjects().contains(sourceObject)) {
                jsonGenerator.writeObjectFieldStart(field.getName());
                jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                jsonGenerator.writeEndObject();
                return;
            }
            
            if (!appendGenericId) {
                identifier = null;
            }
            
            if (processPrimitiveOrWrapperOrString(jsonGenerator, sourceObject, identifier, field)) {
                return;
            }
      
            if (processArrayOrList(field, sourceObject, jsonGenerator, genericSerializerProvider, depth, rootObject, identifier)) {
                return;
            }
            
            if (processMap(field, sourceObject, jsonGenerator, genericSerializerProvider, depth, rootObject, identifier)) {
                return;
            }
            
            processOtherObject(field, sourceObject, jsonGenerator, genericSerializerProvider, depth, rootObject, identifier);
            
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /** TODO */
    private void processOtherObject(Field field, Object sourceObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, String identifier) {
        try {
            if (field != null) {
                jsonGenerator.writeObjectFieldStart(field.getName());
            } else {
                jsonGenerator.writeStartObject();
            }
            if (field == null || !isClassesTheSame(field.getType(), sourceObject.getClass())) {
                jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                genericSerializerProvider.getSerializedObjects().add(sourceObject);
                jsonGenerator.writeObjectFieldStart(GENERIC_VALUE);
            }

            for (Field innerField : getFields(sourceObject)) {
                Object value = getObject(innerField, sourceObject);
                boolean appendGenericId = value != null && !isClassesTheSame(value.getClass(), innerField.getType());
                serializeObject(innerField, value, jsonGenerator, genericSerializerProvider, depth + 1, rootObject, appendGenericId);
            }
            jsonGenerator.writeEndObject();
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /** TODO */
    private boolean processNull(Field field, Object sourceObject, JsonGenerator jsonGenerator) throws IOException {
        if (sourceObject == null) {
            if (field == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeNullField(field.getName());
            }
            return true;
        }
        return false;
    }

    /** TODO Kyrylo Semenko */
    private boolean processArrayOrList(Field field, Object fieldObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, String identifier) {
        try {
            if (isArrayOrCollection(fieldObject)) {
                
                boolean attachIdentifier = identifier != null;
                
                List<Object> objectList = new ArrayList<>();
                addItemsToList(fieldObject, objectList);
                
                if (attachIdentifier) {
                    if (field != null) {
                        jsonGenerator.writeObjectFieldStart(field.getName());
                    } else {
                        jsonGenerator.writeStartObject();
                    }
                    jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                    jsonGenerator.writeArrayFieldStart(GENERIC_VALUE);
                } else {
                    if (field != null) {
                        jsonGenerator.writeArrayFieldStart(field.getName());
                    } else {
                        jsonGenerator.writeStartArray();
                    }
                }
                
                Type[] componentTypes = getContainerType(field, fieldObject);
                for (Object nextObject : objectList) {
                    boolean appendGenericId = nextObject != null && !isClassesTheSame((Class<?>) componentTypes[0], nextObject.getClass());
                    serializeObject(field, nextObject, jsonGenerator, genericSerializerProvider, depth, rootObject, appendGenericId);
                }
                
                jsonGenerator.writeEndArray();
                if (attachIdentifier) {
                    jsonGenerator.writeEndObject();
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new AppRuntimeException(e);
        }
    }
    
    /** TODO Kyrylo Semenko */
    private boolean processMap(Field field, Object fieldObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, String identifier) {
        try {
            if (isMap(fieldObject)) {
                
                boolean attachIdentifier = identifier != null;
                
                List<Object> objectList = new ArrayList<>();
                addItemsToList(fieldObject, objectList);
                
                if (!objectList.isEmpty()) {
                    
                    if (attachIdentifier) {
                        if (field != null) {
                            jsonGenerator.writeObjectFieldStart(field.getName());
                        } else {
                            jsonGenerator.writeStartObject();
                        }
                        jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                        jsonGenerator.writeArrayFieldStart(GENERIC_VALUE);
                    } else {
                        if (field != null) {
                            jsonGenerator.writeArrayFieldStart(field.getName());
                        } else {
                            jsonGenerator.writeStartArray();
                        }
                    }
                    Type[] componentTypes = getContainerType(field, fieldObject);
                    for (Object nextObject : objectList) {
                        boolean appendGenericId = nextObject != null && !isClassesTheSame((Class<?>) componentTypes[0], nextObject.getClass());
                        serializeObject(field, nextObject, jsonGenerator, genericSerializerProvider, depth, rootObject, appendGenericId);
                    }
                    
                    jsonGenerator.writeEndArray();
                    if (attachIdentifier) {
                        jsonGenerator.writeEndObject();
                    }
                } else {
                    // A single entry. Examples:
                    
                    // ["theKey","theValue"]
                    // [{"_a4jId":"java.lang.String@123", "value":"theKey"},"theValue"]
                    // ["theKey",{"_a4jId":"java.lang.String@123", "value":"theValue"}]
                    // [{"_a4jId":"java.lang.String@123", "value":"theKey"},{"_a4jId":"java.lang.String@123", "value":"theValue"}]
                    
                    // {"_a4id": "[I@5237ca27", "value":["theKey","theValue"]}
                    // {"_a4id": "[I@5237ca27", "value":[{"_a4jId":"java.lang.String@123", "value":"theKey"},"theValue"]}
                    // {"_a4id": "[I@5237ca27", "value":["theKey",{"_a4jId":"java.lang.String@123", "value":"theValue"}]}
                    // {"_a4id": "[I@5237ca27", "value":[{"_a4jId":"java.lang.String@123", "value":"theKey"},{"_a4jId":"java.lang.String@123", "value":"theValue"}]}

                    // An example in context of the enclosing object:
                    // {"_a4id":"java.util.TreeMap@370b5","value":[["1","one"],["2","two"]]}
                    if (attachIdentifier) {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                        jsonGenerator.writeArrayFieldStart(GENERIC_VALUE);
                    } else {
                        jsonGenerator.writeStartArray();
                    }
                    
                    serializeObject(null, getEntryKey(fieldObject), jsonGenerator, genericSerializerProvider, depth, rootObject, attachIdentifier);
                    serializeObject(null, getEntryValue(fieldObject), jsonGenerator, genericSerializerProvider, depth, rootObject, attachIdentifier);
                    
                    jsonGenerator.writeEndArray();
                    if (attachIdentifier) {
                        jsonGenerator.writeEndObject();
                    }
                }
                
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new AppRuntimeException(e);
        }
    }

    // TODO Auto-generated method stub
    private Object getEntryValue(Object fieldObject) {
        @SuppressWarnings("rawtypes")
        Map.Entry entry = (Entry) fieldObject;
        return entry.getValue();
    }

    // TODO Auto-generated method stub
    private Object getEntryKey(Object fieldObject) {
        @SuppressWarnings("rawtypes")
        Map.Entry entry = (Entry) fieldObject;
        return entry.getKey();
    }

    /** TODO */
    private boolean isArrayOrCollection(Object fieldObject) {
        if (fieldObject == null) {
            return false;
        }
        if (fieldObject instanceof Field) {
            Field field = (Field) fieldObject;
            if (field.getType().isArray()) {
                return true;
            }
            if (field.getGenericType() instanceof ParameterizedType) {
                return ((ParameterizedType) field.getGenericType()).getActualTypeArguments().length == 1;
            }
            return false;
        }
        return (fieldObject.getClass().getTypeParameters().length == 1 || fieldObject.getClass().isArray());
    }
    
    /** TODO */
    private boolean isMap(Object fieldObject) {
        if (fieldObject == null) {
            return false;
        }
        if (fieldObject instanceof Field) {
            Field field = (Field) fieldObject;
            if (field.getGenericType() instanceof ParameterizedType) {
                return ((ParameterizedType) field.getType().getGenericSuperclass()).getActualTypeArguments().length == 2;
            }
            return false;
        }
        return (fieldObject.getClass().getTypeParameters().length == 2);
    }

    /** TODO */
    private void addItemsToList(Object fieldObject, List<Object> objectList) {
        if (fieldObject.getClass().isArray()) {
            addArrayItemsToList(fieldObject, objectList);
        } else if (Collection.class.isAssignableFrom(fieldObject.getClass())) {
            addCollectionItemsToList(fieldObject, objectList);
        } else if (Map.class.isAssignableFrom(fieldObject.getClass())) {
            addMapItemsToList(fieldObject, objectList);
        }
    }

    // TODO Auto-generated method stub
    private void addMapItemsToList(Object fieldObject, List<Object> objectList) {
        for (Object entry : ((Map<?,?>) fieldObject).entrySet()) {
            objectList.add(entry);
        }
    }

    /** TODO */
    private void addCollectionItemsToList(Object fieldObject, List<Object> objectList) {
        Iterator<?> iterator = ((Iterable<?>) fieldObject).iterator();
        while(iterator.hasNext()) {
            Object nextObject = iterator.next();
            objectList.add(nextObject);
        }
    }

    /** TODO */
    private void addArrayItemsToList(Object fieldObject, List<Object> objectList) {
        int length = Array.getLength(fieldObject);
        for (int i = 0; i < length; i++) {
            Object arrayElement = Array.get(fieldObject, i);
            objectList.add(arrayElement);
        }
    }

    // TODO Auto-generated method stub
    private boolean isClassesTheSame(Class<?> left, Class<?> right) {
        return toWrapper(left) == toWrapper(right);
    }
    
    // TODO
    private Class<?> toWrapper(Class<?> type) {
        if (boolean.class == type) {
            return Boolean.class;
        }
        
        if (byte.class == type) {
            return Byte.class;
        }
        
        if (short.class == type) {
            return Short.class;
        }
        
        if (int.class == type) {
            return Integer.class;
        }
        
        if (long.class == type) {
            return Long.class;
        }
        
        if (float.class == type) {
            return Float.class;
        }
        
        if (double.class == type) {
            return Double.class;
        }
        
        return type;
    }

    /** TODO Kyrylo Semenko  */
    private Type[] getContainerType(Field field, Object fieldObject) {
        if (fieldObject != null) {
            // Arrays
            if (fieldObject.getClass().isArray()) {
                return new Type[]{(fieldObject.getClass().getComponentType())};
            }
            
            // Collections and maps
            List<Object> objectList = new ArrayList<>();
            addItemsToList(fieldObject, objectList);
            
            // Iterate objects and find out it types, then choose the most generic
            Class<?> result = null;
            for (Object object : objectList) {
                if (result == null) {
                    result = object.getClass();
                } else {
                    if (object.getClass().isAssignableFrom(result)) {
                        result = object.getClass();
                    } else if (!result.isAssignableFrom(object.getClass())) {
                        throw new AppRuntimeException("One collection or map should not have items with different types");
                    }
                }
            }
            return new Type[]{result};
        }
        if (field != null) {
            // Arrays
            if (field.getType().isArray()) {
                return new Type[]{(field.getType().getComponentType())};
            }
            // Collections and Maps
            Type type = field.getGenericType();
            if (type instanceof ParameterizedType) {
                return ((ParameterizedType) type).getActualTypeArguments();
            }
        }
        // Other objects
        throw new AppRuntimeException("The field '" + field + "' is not an Array nor ParameterizedType");
    }

    /**
     * If the object is primitive or wrapper or String, add its value to jsonGenerator and return 'true',
     * else do nothing and return 'false';
     * @param jsonGenerator the target object
     * @param object the source object
     * @param identifier if not null, a {@link #GENERIC_KEY_ID} and {@link #GENERIC_VALUE_SEPARATOR} will be inserted to JSON
     * @param field if not null, used for comparison of object type and field type
     */
    private boolean processPrimitiveOrWrapperOrString(JsonGenerator jsonGenerator, Object object, String identifier, Field field) throws IOException {
        if (ClassUtils.isPrimitiveOrWrapper(object.getClass()) || object instanceof String) {
            
            boolean classesTheSame = field != null && isClassesTheSame(field.getType(), object.getClass());
            
            if (identifier != null && !classesTheSame) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                jsonGenerator.writeStringField(GENERIC_VALUE, object.toString());
                jsonGenerator.writeEndObject();
            } else {
                if (isArrayOrCollection(field)) {
                    jsonGenerator.writeString(object.toString());
                } else {
                    if (field != null) {
                        jsonGenerator.writeStringField(field.getName(), object.toString());
                    } else {
                        jsonGenerator.writeString(object.toString());
                    }
                }
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
        // TODO zvazit pridani generic, napriklad misto
        // java.util.TreeMap@370b5
        // bude
        // java.util.TreeMap<int,java.lang.String>@370b5
        return value.getClass().getName() + GENERIC_VALUE_SEPARATOR + Integer.toHexString(value.hashCode());
    }

}
