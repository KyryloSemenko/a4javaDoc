package com.apache.a4javadoc.javaagent.mapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
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
 * Each object in JSON will have a property with key {@link #GENERIC_KEY_ID} and value {@link IdentifierService#generateIdentifier(Object)}.
 * @author Kyrylo Semenko
 */
@SuppressWarnings("serial")
public class GenericSerializer extends StdSerializer<Object> {
    private static final Logger logger = LoggerFactory.getLogger(GenericSerializer.class);
    
    private static final String GETTER_PREFIX = "get";

    /** The key of a primitive or wrapper value */
    public static final String GENERIC_VALUE = "value";
    
    /** Each serialized object will have this key with value {@link IdentifierService#generateIdentifier(Object)} */
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
            
            String identifier = IdentifierService.getInstance().generateIdentifier(sourceObject);
            
            if (genericSerializerProvider.getSerializedObjects().contains(sourceObject)) {
                if (field != null) {
                    jsonGenerator.writeObjectFieldStart(field.getName());
                } else {
                    jsonGenerator.writeStartObject();
                }
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
            
            if (processMapEntry(field, sourceObject, jsonGenerator, genericSerializerProvider, depth, rootObject, identifier)) {
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
            //header
            if (field != null) {
                debugJsonGenerator(jsonGenerator);
                jsonGenerator.writeObjectFieldStart(field.getName());
            } else {
                jsonGenerator.writeStartObject();
            }
            // body
            if (identifier != null
                    && (field == null || !ClassService.getInstance().isClassesTheSame(field.getType(), sourceObject.getClass()))) {
                jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                genericSerializerProvider.getSerializedObjects().add(sourceObject);
                jsonGenerator.writeObjectFieldStart(GENERIC_VALUE);
            }
            // TODO Kyrylo Semenko zde je potreba zvolit strategii. Bud hledat factory method a konstruktor, nebo iterovat fieldy.
//            ConstructorService.getInstance().
            for (Field innerField : FieldService.getInstance().getFields(sourceObject)) {
                Object value = getObject(innerField, sourceObject);
                boolean appendGenericId = value != null && !ClassService.getInstance().isClassesTheSame(value.getClass(), innerField.getType());
                serializeObject(innerField, value, jsonGenerator, genericSerializerProvider, depth + 1, rootObject, appendGenericId);
            }
            // footer
            if (identifier != null) {
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndObject();
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /** TODO smazat, protoze je jen pro debug */
    private void debugJsonGenerator(JsonGenerator jsonGenerator) {
        try {
            Field field = jsonGenerator.getClass().getDeclaredField("_outputBuffer");
            field.setAccessible(true);
            char[] charBuffer = (char[]) field.get(jsonGenerator);
            System.out.println(new String(charBuffer));
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
                BundleService.getInstance().addItemsToList(fieldObject, objectList);
                
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
                
                List<Class<?>> componentTypes = FieldService.getInstance().getContainerTypes(field, fieldObject, null);
                for (Object nextObject : objectList) {
                    boolean appendGenericId = nextObject != null && !ClassService.getInstance().isClassesTheSame(componentTypes.get(0), nextObject.getClass()); // TODO je to nesmysl
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
    /* 
                 Examples of a single entry:
                
                 ["theKey","theValue"]
                 [{"_a4jId":"java.lang.String@123", "value":"theKey"},"theValue"]
                 ["theKey",{"_a4jId":"java.lang.String@123", "value":"theValue"}]
                 [{"_a4jId":"java.lang.String@123", "value":"theKey"},{"_a4jId":"java.lang.String@123", "value":"theValue"}]
                
                 {"_a4id": "[I@5237ca27", "value":["theKey","theValue"]}
                 {"_a4id": "[I@5237ca27", "value":[{"_a4jId":"java.lang.String@123", "value":"theKey"},"theValue"]}
                 {"_a4id": "[I@5237ca27", "value":["theKey",{"_a4jId":"java.lang.String@123", "value":"theValue"}]}
                 {"_a4id": "[I@5237ca27", "value":[{"_a4jId":"java.lang.String@123", "value":"theKey"},{"_a4jId":"java.lang.String@123", "value":"theValue"}]}

                 An example in context of the enclosing object:
                 {"_a4id":"java.util.TreeMap@370b5","value":[["1","one"],["2","two"]]}
     */
    private boolean processMap(Field field, Object fieldObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, String identifier) {
        try {
            if (isMap(fieldObject)) {
                
                boolean attachIdentifier = identifier != null;
                
                List<Object> objectList = new ArrayList<>();
                BundleService.getInstance().addItemsToList(fieldObject, objectList);
                
                if (attachIdentifier) {
                    debugJsonGenerator(jsonGenerator);
                    if (field != null) {
                        jsonGenerator.writeObjectFieldStart(field.getName());
                    } else {
                        jsonGenerator.writeStartObject();
                    }
                    jsonGenerator.writeStringField(GENERIC_KEY_ID, identifier);
                    jsonGenerator.writeArrayFieldStart(GENERIC_VALUE);
                } else {
                    jsonGenerator.writeStartArray();
                }

                if (!objectList.isEmpty()) {
                    // for example TreeMap<K,V>
                    List<Class<?>> componentTypes = FieldService.getInstance().getContainerTypes(field, fieldObject, null);
                    for (Object nextObject : objectList) {
                        boolean appendGenericId = nextObject != null && !ClassService.getInstance().isClassesTheSame(componentTypes.get(0), nextObject.getClass()); // TODO je to nesmysl
                        serializeObject(field, nextObject, jsonGenerator, genericSerializerProvider, depth, rootObject, appendGenericId);
                    }
                } else {
                    serializeObject(null, fieldObject, jsonGenerator, genericSerializerProvider, depth, rootObject, attachIdentifier);
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
    
    private boolean processMapEntry(Field field, Object fieldObject, JsonGenerator jsonGenerator, GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, String identifier) {
        try {
            if (fieldObject instanceof Map.Entry<?, ?>) {
                
                boolean attachIdentifier = identifier != null;
                
                if (attachIdentifier) {
                    debugJsonGenerator(jsonGenerator);
                    if (field != null) {
                        jsonGenerator.writeObjectFieldStart(field.getName());
                    } else {
                        jsonGenerator.writeStartObject();
                    }
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
                
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new AppRuntimeException(e);
        }
    }

    // TODO Auto-generated method stub
    private Object getEntryValue(Object fieldObject) {
        Map.Entry<?, ?> entry = (Entry<?, ?>) fieldObject;
        return entry.getValue();
    }

    // TODO Auto-generated method stub
    private Object getEntryKey(Object fieldObject) {
        Map.Entry<?, ?> entry = (Entry<?, ?>) fieldObject;
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
    
    /**
     * If the argument is {@link Field} and its {@link Field#getGenericType()} has two parameters, return 'true'.<br>
     * Else if the argument is instanceof {@link Map}, return 'true'.<br>
     * Else return 'false'.
     * @param instanceOrField {@link Field} or instance of some object.
     * @return 'true' if the {@link Field} represents a {@link Map} or the instance is a {@link Map}.
     */
    private boolean isMap(Object instanceOrField) {
        if (instanceOrField == null) {
            return false;
        }
        if (instanceOrField instanceof Field) {
            Field field = (Field) instanceOrField;
            if (field.getGenericType() instanceof ParameterizedType) {
                return ((ParameterizedType) field.getType().getGenericSuperclass()).getActualTypeArguments().length == 2;
            }
            return false;
        }
        return (instanceOrField instanceof Map);
    }

    /**
     * If the object is primitive or wrapper or String, add its value to jsonGenerator and return 'true',
     * else do nothing and return 'false';
     * @param jsonGenerator the target object
     * @param object the source object
     * @param identifier if not null, a {@link #GENERIC_KEY_ID} and {@link IdentifierService#GENERIC_VALUE_SEPARATOR} will be inserted to JSON
     * @param field if not null, used for comparison of object type and field type
     */
    private boolean processPrimitiveOrWrapperOrString(JsonGenerator jsonGenerator, Object object, String identifier, Field field) throws IOException {
        if (ClassUtils.isPrimitiveOrWrapper(object.getClass()) || object instanceof String) {
            
            boolean classesTheSame = field != null && ClassService.getInstance().isClassesTheSame(field.getType(), object.getClass());
            
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

}
