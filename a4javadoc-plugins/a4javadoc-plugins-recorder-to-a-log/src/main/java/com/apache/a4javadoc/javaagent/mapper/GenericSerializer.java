package com.apache.a4javadoc.javaagent.mapper;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * The class serializes object graph to JSON. It prevents circular dependencies.<br>
 * It uses {@link GenericSerializerProvider}.<br>
 * Each object in JSON will have a property with key {@link #GENERIC_KEY_ID}
 * and value
 * {@link IdentifierService#generateIdentifier(com.fasterxml.jackson.databind.JsonNode, Object)}.
 * @author Kyrylo Semenko
 */
@SuppressWarnings("serial")
public class GenericSerializer extends StdSerializer<Object> {
    private static final Logger logger = LoggerFactory.getLogger(GenericSerializer.class);
    
    private static final String GETTER_PREFIX = "get";

    /** The key of a primitive or wrapper value */
    public static final String GENERIC_VALUE = "value";
    
    /**
     * Each serialized object will have this key with value
     * {@link IdentifierService#generateIdentifier(com.fasterxml.jackson.databind.JsonNode, Object)}
     */
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

    /**
     * Depends on a sourceObject type apply one of serialization methods.
     * This method is called recursively.
     * 
     * @param field can be 'null'. If exists, its {@link Field#getName()}
     * will be used as a key in JSON
     * @param sourceObject this object will be serialized to JSON
     * @param jsonGenerator JSON holder
     * @param genericSerializerProvider object that contains this serialization
     * state
     * @param depth current depth of plunging if the rootObject graph
     * @param rootObject the original whole object for serialization
     * @param attachIdentifier if exists, it defines obligation
     * of attaching an identifier of sourmceObject to JSON
     */
    private void serializeObject(Field field, Object sourceObject, JsonGenerator jsonGenerator,
            GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject,
            Boolean attachIdentifier) {
        try {
            if (processNull(field, sourceObject, jsonGenerator)) {
                return;
            }
            
            if (sourceObject instanceof Identifier) {
                IdentifierService.getInstance().processIdentifier((Identifier) sourceObject, jsonGenerator);
                return;
            }
            
            Identifier identifier = IdentifierService.getInstance().createIdentifier(sourceObject);
            if (Boolean.TRUE.equals(attachIdentifier)) {
                identifier.setRequiresToBeIncludedInJson(attachIdentifier);
            }
            
            if (genericSerializerProvider.getSerializedObjects().contains(sourceObject)) {
                writeExiststingObject(field, jsonGenerator, identifier);
                return;
            }
            
            if (processPrimitiveOrWrapperOrString(jsonGenerator, sourceObject, identifier, field)) {
                return;
            }
      
            if (processArrayOrCollection(field, sourceObject, jsonGenerator, genericSerializerProvider, depth,
                    rootObject, identifier)) {
                return;
            }
            
            if (processMap(field, sourceObject, jsonGenerator, genericSerializerProvider, depth, rootObject,
                    identifier)) {
                return;
            }
            
            if (processMapEntry(field, sourceObject, jsonGenerator, genericSerializerProvider, depth, rootObject,
                    identifier)) {
                return;
            }
            
            processComplexObject(field, sourceObject, jsonGenerator, genericSerializerProvider, depth, rootObject,
                    identifier);
            
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * Append a reference to existing object to {@link JsonGenerator}.
     * 
     * @param field can be 'null'. If it exists, its {@link Field#getName()}
     * will be used as a key of the object
     * @param jsonGenerator where the reference to existing object
     * will be stored as JSON
     * @param identifier reference to an existing object
     * in the {@link JsonGenerator}
     * @throws IOException
     */
    private void writeExiststingObject(Field field, JsonGenerator jsonGenerator, Identifier identifier)
            throws IOException {
        if (field != null) {
            jsonGenerator.writeObjectFieldStart(field.getName());
        } else {
            jsonGenerator.writeStartObject();
        }
        
        jsonGenerator.writeObjectField(GENERIC_KEY_ID, identifier);
        jsonGenerator.writeEndObject();
    }
    
    /**
     * Create JSON from instance of complex object graph.
     * 
     * @param field if not 'null', its {@link Field#getName()} will be used
     * as a key of HSON field
     * @param sourceObject the part of the rootObject to be serialized to JSON
     * @param jsonGenerator JSON holder
     * @param genericSerializerProvider serialization state
     * @param depth plunging depth of the sourceObject within the rootObject
     * @param rootObject the serialized object graph root
     * @param identifier the {@link Identifier} of the sourceObject
     */
    private void processComplexObject(Field field, Object sourceObject, JsonGenerator jsonGenerator,
            GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, Identifier identifier) {
        try {
            //header
            
            if (field != null) {
                debugJsonGenerator(jsonGenerator);
                jsonGenerator.writeObjectFieldStart(field.getName());
            } else {
                jsonGenerator.writeStartObject();
            }
            
            // body
            
            if (identifier.isRequiresToBeIncludedInJson()) {
                jsonGenerator.writeObjectField(GENERIC_KEY_ID, identifier);
                genericSerializerProvider.getSerializedObjects().add(sourceObject);
                jsonGenerator.writeObjectFieldStart(GENERIC_VALUE);
            }
            if (identifier.getContainerType().getDisassembleMethod() != null) {
                jsonGenerator.writeStartArray();
                
                jsonGenerator.writeEndArray();
            } else {
                for (Field innerField : FieldService.getInstance().getFields(sourceObject)) {
                    Object value = getObject(innerField, sourceObject);
                    boolean appendIdentifier = value != null &&
                            !ClassService.getInstance().classesAreTheSame(value.getClass(), innerField.getType());
                    serializeObject(innerField, value, jsonGenerator, genericSerializerProvider, depth + 1,
                            rootObject, appendIdentifier);
                }
            }
            
            // footer
            
            if (identifier.isRequiresToBeIncludedInJson()) {
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndObject();
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * Temporary method for debugging purposes.
     * Will be removed together with the {@link JsonGenerator}.
     */
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

    /**
     * Create JSON with value 'null' if the sourceObject is 'null'.
     * 
     * @param field if not 'null', its {@link Field#getName()} will be used
     * as a JSON field key
     * @param sourceObject the data source
     * @param jsonGenerator the JSON holder
     * @return 'true" if the sourceObject is 'null'. Else return 'false'
     */
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

    /**
     * If the sourceObject {@link #isArrayOrCollection(Object)}, then serialize
     * it to JSON and return 'true'.
     * 
     * @param field if not 'null' it will be used in the
     * {@link #jsonStartArray(Field, JsonGenerator, Identifier)} method
     * @param sourceObject the data source to be serialized to JSON
     * @param jsonGenerator the JSON holder
     * @param genericSerializerProvider this serialization state
     * @param depth the plunging depth of the sourceObject
     * within the rootObject
     * @param rootObject the root of the sourceObject
     * @param identifier the {@link Identifier} of the sourceObject
     */
    private boolean processArrayOrCollection(Field field, Object sourceObject, JsonGenerator jsonGenerator,
            GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, Identifier identifier) {
        try {
            if (isArrayOrCollection(sourceObject)) {
                
                List<Object> itemList = new ArrayList<>();
                BundleService.getInstance().addItemsToList(sourceObject, itemList);
                
                jsonStartArray(field, jsonGenerator, identifier);
                
                Class<?> itemType = identifier.getContainerType().getContainerTypes().get(0).getObjectClass();
                for (Object nextObject : itemList) {
                    boolean appendGenericId = nextObject != null &&
                            !ClassService.getInstance().classesAreTheSame(itemType, nextObject.getClass());
                    serializeObject(field, nextObject, jsonGenerator, genericSerializerProvider,
                            depth, rootObject, appendGenericId);
                }
                
                jsonGenerator.writeEndArray();
                if (identifier.isRequiresToBeIncludedInJson()) {
                    jsonGenerator.writeEndObject();
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new AppRuntimeException(e);
        }
    }
    
    /**
     * <p>
     * Create JSON from a {@link Map}.
     * 
     * <p>
     * Examples of a single entry:
     * <pre>
                
         ["theKey","theValue"]
         [{"_a4jId":"java.lang.String", "value":"theKey"},"theValue"]
         ["theKey",{"_a4jId":"java.lang.String", "value":"theValue"}]
         [{"_a4jId":"java.lang.String", "value":"theKey"},{"_a4jId":"java.lang.String", "value":"theValue"}]
        
         {"_a4id": "[I", "value":["theKey","theValue"]}
         {"_a4id": "[I", "value":[{"_a4jId":"java.lang.String", "value":"theKey"},"theValue"]}
         {"_a4id": "[I", "value":["theKey",{"_a4jId":"java.lang.String", "value":"theValue"}]}
         {"_a4id": "[I", "value":[{"_a4jId":"java.lang.String", "value":"theKey"},{"_a4jId":"java.lang.String", "value":"theValue"}]}
    
         An example in context of the enclosing object:
         {"_a4id":"java.util.TreeMap","value":[["1","one"],["2","two"]]}
         
     * </pre>
     */
    private boolean processMap(Field field, Object fieldObject, JsonGenerator jsonGenerator,
            GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, Identifier identifier) {
        try {
            if (isMap(fieldObject)) {
                
                List<Object> objectList = new ArrayList<>();
                BundleService.getInstance().addItemsToList(fieldObject, objectList);
                
                jsonStartArrayWithoutKey(field, jsonGenerator, identifier);

                if (!objectList.isEmpty()) {
                    // for example TreeMap<K,V>
                    List<Class<?>> componentTypes = FieldService.getInstance().getContainerTypes(field, fieldObject);
                    for (Object nextObject : objectList) {
                        ClassService classService = ClassService.getInstance();
                        boolean appendGenericId = nextObject != null &&
                                !classService.classesAreTheSame(componentTypes.get(0), nextObject.getClass());
                        serializeObject(field, nextObject, jsonGenerator, genericSerializerProvider, depth,
                                rootObject, appendGenericId);
                    }
                } else {
                    serializeObject(null, fieldObject, jsonGenerator, genericSerializerProvider, depth, rootObject,
                            identifier.isRequiresToBeIncludedInJson());
                }
                jsonGenerator.writeEndArray();
                if (identifier.isRequiresToBeIncludedInJson()) {
                    jsonGenerator.writeEndObject();
                }
                
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * <p>
     * Create a header of a JSON array.
     * 
     * <p>
     * If the {@link Identifier#isRequiresToBeIncludedInJson()} is 'true', call
     * the
     * {@link #jsonStartArrayWithIdentifier(Field, JsonGenerator, Identifier)}
     * method.
     * 
     * <p>
     * Else depends on the {@link Field} argument. If this argument is not
     * 'null', create JSON object start without a key. Else with a key.
     * 
     * @param field if not 'null', its {@link Field#getName()} will be used
     * as a JSON key
     * @param jsonGenerator the JSON holder
     * @param identifier if {@link Identifier#isRequiresToBeIncludedInJson()}
     * is 'true', create JSON header with {@link Identifier}.
     * @throws IOException
     */
    private void jsonStartArray(Field field, JsonGenerator jsonGenerator, Identifier identifier) throws IOException {
        if (identifier.isRequiresToBeIncludedInJson()) {
            jsonStartArrayWithIdentifier(field, jsonGenerator, identifier);
        } else {
            if (field != null) {
                jsonGenerator.writeArrayFieldStart(field.getName());
            } else {
                jsonGenerator.writeStartArray();
            }
        }
    }

    /**
     * <p>
     * If the {@link Field} is not 'null', create a JSON object key with
     * {@link Field#getName()} value, for example
     * <pre>
     * "myField" = {
     * </pre>
     * else just write an object start tag '{'.
     * 
     * <p>
     * Add the {@link Identifier} and a following array start tag with
     * {@link #GENERIC_VALUE} name, for example
     * <pre>
        "myField" = {
            "_a4id": {
                "containerType": {
                    "objectClass": "com.apache.a4javadoc.javaagent.mapper.WrapperClass",
                    "containerTypes": []
                }
            },
            "value": [
     * </pre>
     * 
     * @param field if not 'null', its {@link Field#getName()}
     * will be used as a JSON object key.
     * @param jsonGenerator the JSON holder
     * @param identifier the {@link Identifier} will be inserted to JSON
     * @throws IOException
     */
    private void jsonStartArrayWithIdentifier(Field field, JsonGenerator jsonGenerator, Identifier identifier)
            throws IOException {
        
        debugJsonGenerator(jsonGenerator);
        if (field != null) {
            jsonGenerator.writeObjectFieldStart(field.getName());
        } else {
            jsonGenerator.writeStartObject();
        }
        jsonGenerator.writeObjectField(GENERIC_KEY_ID, identifier);
        jsonGenerator.writeArrayFieldStart(GENERIC_VALUE);
    }

    /**
     * <p>
     * Write the part of JSON to the {@link JsonGenerator}.
     * 
     * <p>
     * If the {@link Identifier#isRequiresToBeIncludedInJson()}, then call the
     * {@link #jsonStartArrayWithIdentifier(Field, JsonGenerator, Identifier)}
     * method. Else write '[' only.
     * 
     * @param field will be propagated to the
     * {@link #jsonStartArrayWithIdentifier(Field, JsonGenerator, Identifier)}
     * method.
     * @param jsonGenerator the JSON holder
     * @param identifier contains information about serialized object state
     * @throws IOException
     */
    private void jsonStartArrayWithoutKey(Field field, JsonGenerator jsonGenerator, Identifier identifier)
            throws IOException {
        
        if (identifier.isRequiresToBeIncludedInJson()) {
            jsonStartArrayWithIdentifier(field, jsonGenerator, identifier);
        } else {
            jsonGenerator.writeStartArray();
        }
    }
    
    /**
     * <p>
     * If the sourceObject is {@link Entry}, create JSON from it
     * and return 'true'. Else do nothing and return 'false'.
     * 
     * <p>
     * Call the
     * {@link #jsonStartArrayWithoutKey(Field, JsonGenerator, Identifier)}
     * method, then serialize {@link Entry#getKey()} and
     * {@link Entry#getValue()} and finally write the '}' tag.
     * 
     * @param field will be propagated to the
     * {@link #jsonStartArrayWithoutKey(Field, JsonGenerator, Identifier)}
     * method
     * @param sourceObject the key and value holder
     * @param jsonGenerator the JSON holder
     * @param genericSerializerProvider the serialization state holder
     * @param depth plunging depth of the sourceObject within the rootObject
     * @param identifier the sourceObject state holder
     * @return 'true' if the sourceObject is {@link Entry} and it is serialized
     */
    private boolean processMapEntry(Field field, Object sourceObject, JsonGenerator jsonGenerator,
            GenericSerializerProvider genericSerializerProvider, int depth, Object rootObject, Identifier identifier) {
        try {
            if (sourceObject instanceof Map.Entry<?, ?>) {
                
                jsonStartArrayWithoutKey(field, jsonGenerator, identifier);
                
                Object key = ((Entry<?, ?>) sourceObject).getKey();
                serializeObject(null, key, jsonGenerator, genericSerializerProvider, depth,
                        rootObject, identifier.isRequiresToBeIncludedInJson());
                
                Object value = ((Entry<?, ?>) sourceObject).getValue();
                serializeObject(null, value, jsonGenerator, genericSerializerProvider, depth,
                        rootObject, identifier.isRequiresToBeIncludedInJson());

                jsonGenerator.writeEndArray();
                if (identifier.isRequiresToBeIncludedInJson()) {
                    jsonGenerator.writeEndObject();
                }
                
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new AppRuntimeException(e);
        }
    }
    
    /**
     * Find the type of an object represented by the
     * {@link Field} from the argument.
     * 
     * @param field a {@link Field} for exploration
     * @return 'true' if the field represents an {@link Array} or
     * a {@link Collection}
     * Else return 'false'.
     */
    private boolean isFieldArrayOrCollection(Field field) {
        if (field == null) {
            return false;
        }
        return field.getType().isArray() || Collection.class.isAssignableFrom(field.getType());
    }

    /**
     * Find the type of the argument.
     * 
     * @param sourceObject an object for exploration
     * @return 'true' if the sourceObject is an {@link Array}
     * or a {@link Collection}.
     * Else return 'false'.
     */
    private boolean isArrayOrCollection(Object sourceObject) {
        if (sourceObject == null) {
            return false;
        }
        return sourceObject.getClass().isArray() || Collection.class.isAssignableFrom(sourceObject.getClass());
    }
    
    /**
     * If the argument is {@link Field} and its {@link Field#getGenericType()} has two parameters, return 'true'.<br>
     * Else if the argument is instance of {@link Map}, return 'true'.<br>
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
     * @param identifier the object {@link Identifier}
     * @param field if not null, used for comparison of object type and field type
     */
    private boolean processPrimitiveOrWrapperOrString(JsonGenerator jsonGenerator, Object object,
            Identifier identifier, Field field) throws IOException {
        if (ClassService.getInstance().isPrimitiveOrWrapperOrString(object)) {
            
            if (identifier.isRequiresToBeIncludedInJson()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField(GENERIC_KEY_ID, identifier);
                jsonGenerator.writeStringField(GENERIC_VALUE, object.toString());
                jsonGenerator.writeEndObject();
            } else {
                if (isFieldArrayOrCollection(field)) {
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
