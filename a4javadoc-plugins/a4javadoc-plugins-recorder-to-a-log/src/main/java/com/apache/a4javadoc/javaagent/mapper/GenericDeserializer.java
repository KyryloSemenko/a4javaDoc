package com.apache.a4javadoc.javaagent.mapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * This deserializer can process JSON string created by {@link GenericSerializer}.<br>
 * It can process circular references in JSON, where some object contains itself somewhere in its object graph.<br>
 * @author Kyrylo Semenko
 */
@SuppressWarnings("serial")
public class GenericDeserializer extends StdDeserializer<Object> {
    
    /**
     * Values of this map contains already deserialized objects. The keys of the map contains generic identifiers,
     * see the {@link IdentifierService#generateIdentifier(Object)} method.
     */
    private transient Map<String, Object> deserializedObjects;
    
    /** The default constructor */
    public GenericDeserializer() {
        this(null); 
        deserializedObjects = new HashMap<>();
    } 
 
    /**
     * Type of values this deserializer handles. In our case it is {@link Object}.
     * @param valueClass
     */
    public GenericDeserializer(Class<?> valueClass) { 
        super(valueClass); 
        deserializedObjects = new HashMap<>();
    }
 
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return null;
//        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
//        
//        if (rootNode.get(GenericSerializer.GENERIC_KEY_ID) == null) {
//            throw new AppRuntimeException("Root node without defined field with key '"
//                    + GenericSerializer.GENERIC_KEY_ID
//                    + "' is not accepted. Is this JSON string serialized by '"
//                    + GenericSerializer.class.getCanonicalName() + "'?. RootNode: " + rootNode);
//        }
//        
//        String identifierString = rootNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
//        Identifier identifier = IdentifierService.getInstance().parse(identifierString);
//        if (deserializedObjects.containsKey(identifier)) {
//            return deserializedObjects.get(identifier);
//        }
//        String className = IdentifierService.getInstance().findClassName(identifier);
//        Class<?> clazz = Class.forName(className);
//        JsonNode currentNode = currentNode.get(GenericSerializer.GENERIC_VALUE);
//        
//        return deserializeObject(currentNode, null, null, null, null, null);
    }
//
//    /**
//     * Create a new instance from the currentNode
//     * @param currentNode the source of data
//     * @param defaultType {@link Class} of the new instance
//     * @param containerTypes if the currentNode represents a {@link Iterable} or {@link Map} or other generic type, contains the types {@link Class}es.
//     * @param parentInstance deserialized parent
//     */
//    private Object deserializeObject(JsonNode currentNode, Class<?> defaultType, ContainerType containerType, Object parentInstance) {
//        try {
//            Object instance = null;
//            Class<?> clazz = null;
//            String identifier = defaultIdentifier;
//            if (currentNode.has(GenericSerializer.GENERIC_KEY_ID)) {
//                identifier = currentNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
//                if (deserializedObjects.containsKey(identifier)) {
//                    return deserializedObjects.get(identifier);
//                }
//                String className = IdentifierService.getInstance().findClassName(identifier);
//                clazz = Class.forName(className);
//                currentNode = currentNode.get(GenericSerializer.GENERIC_VALUE);
//            } else {
////                if (containerTypes != null && !containerTypes.isEmpty()) {
////                    // generic or array
////                    for (int i = 0; i < currentNode.size(); i++) {
////                        
////                    }
////                } else {
//                    clazz = defaultType;
////                }
//            }
//            Object result = processPrimitiveOrWrapperOrString(currentNode, clazz);
//            if (result != null) {
//                return result;
//            }
//            instance = instantiate(clazz, containerTypes, currentNode, parentInstance, fieldName, identifier);
//            if (identifier != null) {
//                deserializedObjects.put(identifier, instance);
//            }
//            
//            deserializeSubFields(currentNode, instance, null, identifier);
//            return instance;
//        } catch (Exception e) {
//            throw new AppRuntimeException(e);
//        }
//    }

}
