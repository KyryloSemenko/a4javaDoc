package com.apache.a4javadoc.javaagent.mapper;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.reflect.TypeUtils;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/** 
 * @author Kyrylo Semenko
 */
public class GenericDeserializer extends StdDeserializer<Object> {
    private transient Map<String, Object> deserializedObjects;
    
    public GenericDeserializer() {
        this(null); 
        deserializedObjects = new HashMap<>();
    } 
 
    public GenericDeserializer(Class<?> vc) { 
        super(vc); 
        deserializedObjects = new HashMap<>();
    }
 
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) 
      throws IOException {
        try {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            if (rootNode.isNull()) {
                return null;
            }
            
            if (rootNode.get(GenericSerializer.GENERIC_KEY_ID) == null) {
                throw new AppRuntimeException("Root node without defined field with key '" + GenericSerializer.GENERIC_KEY_ID + "' is not accepted. RootNode: " + rootNode);
            }
            
            String a4id = rootNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
            String className = a4id.substring(0, a4id.indexOf(GenericSerializer.GENERIC_VALUE_SEPARATOR));
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.newInstance();
            if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
                Method valueOf = clazz.getDeclaredMethod("valueOf", String.class);
                String jsonValue = rootNode.get("value").textValue();
                valueOf.invoke(instance, jsonValue);
                return instance;
            }   
            deserializedObjects.put(a4id, instance);
           
            deserializeSubFields(rootNode, instance, null);
            return instance;
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }
    
    /**  */
    private void deserializeSubFields(JsonNode jsonNode, Object parentInstance, String parentFieldName) {
        try {
            Iterator<String> fieldNames = jsonNode.fieldNames();
            List<String> fieldNameList = new ArrayList<>();
            while (fieldNames.hasNext()) {
                fieldNameList.add(fieldNames.next());
            }
            if (fieldNameList.size() == 1) {
                String a4id = jsonNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
                Object value = deserializedObjects.get(a4id);
                setToParent(parentInstance, parentFieldName, value);
                return;
            }
            for (String fieldName : fieldNameList) {
                if (GenericSerializer.GENERIC_KEY_ID.equals(fieldName)) {
                    continue;
                }
                JsonNode fieldNode = jsonNode.get(fieldName);
                if (fieldNode.get(GenericSerializer.GENERIC_KEY_ID) == null) {
                    throw new AppRuntimeException("Root node without defined field with key '" + GenericSerializer.GENERIC_KEY_ID + "' is not accepted. RootNode: " + fieldNode);
                }
                String a4id = fieldNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
                String className = a4id.substring(0, a4id.indexOf(GenericSerializer.GENERIC_VALUE_SEPARATOR));
                Class<?> clazz = Class.forName(className);
                Object instance = null;
                if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
                    String jsonValue = fieldNode.get(GenericSerializer.PRIMITIVE_OR_WRAPPER_VALUE).textValue();
                    Constructor<?> constructor = clazz.getConstructor(String.class);
                    instance = constructor.newInstance(jsonValue);
                } else {
                    instance = clazz.newInstance();
                    deserializeSubFields(fieldNode, instance, fieldName);
                }
                setToParent(parentInstance, fieldName, instance);
            }
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**  */
    private void setToParent(Object parentInstance, String fieldName, Object value) {
        try {
            for (Method method : parentInstance.getClass().getMethods()) {
                if (method.getName().startsWith("set")
                        && method.getName().length() == (fieldName.length() + 3)
                        && method.getName().toLowerCase().endsWith(fieldName.toLowerCase())) {
                    try {
                        method.invoke(parentInstance, value);
                        return;
                    } catch (Exception e) {
                        break;
                    }
                }
            }
            Field field = parentInstance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(parentInstance, value);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

//    /**  */
//    private Object getPrimitiveOrWrappedValue(String fieldName, Object instance, JsonNode jsonNode) {
//        if (jsonNode.isBoolean()) {
//            return jsonNode.asBoolean();
//        }
//        throw new AppRuntimeException("Unimplemented primitive or wrapped jsonNode: " + jsonNode.getNodeType()); 
//    }
//
//    /**  */
//    private Object deserializeFields(JsonNode rootNode, String a4id) {
//        try {
//            String className = a4id.substring(0, a4id.indexOf(GenericSerializer.GENERIC_VALUE_SEPARATOR));
//            Class<?> clazz = Class.forName(className);
//            Object instance = clazz.newInstance();
//            if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
//                Method valueOf = clazz.getDeclaredMethod("valueOf", String.class);
//                String jsonValue = rootNode.get("value").textValue();
//                valueOf.invoke(instance, jsonValue);
//                return instance;
//            }
//            deserializedObjects.put(a4id, instance);
//            Iterator<String> fieldNames = rootNode.fieldNames();
//            while (fieldNames.hasNext()) {
//                String fieldName = fieldNames.next();
//                if (fieldName.equals(GenericSerializer.GENERIC_KEY_ID)) {
//                    continue;
//                }
//                setFieldValueToInstance(fieldName, instance, rootNode.get(fieldName));
//            }
//            return instance;
//        } catch (Exception e) {
//            throw new AppRuntimeException(e);
//        }
//    }
//
//    // TODO Auto-generated method stub
//    /**
//     * Recursive
//     */
//    private void setFieldValueToInstance(String fieldName, Object parentInstance, JsonNode jsonNode) {
//        if (jsonNode.isObject()) {
//            String a4id = jsonNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
//            if (deserializedObjects.containsKey(a4id)) {
//                setObjectValue(fieldName, parentInstance, deserializedObjects.get(a4id));
//                return;
//            }
//            deserializeFields(jsonNode, a4id);
//        } else {
//            setPrimitiveOrWrappedValue(fieldName, parentInstance, jsonNode.get(fieldName));
//        }
//    }
//
//    /**  */
//    private void setPrimitiveOrWrappedValue(String fieldName, Object instance, JsonNode jsonNode) {
//        Method valueOf = clazz.getDeclaredMethod("valueOf", String.class);
//        String jsonValue = rootNode.get("value").textValue();
//        valueOf.invoke(instance, jsonValue);
//        return instance;
//    }
//
//    /**  */
//    private void setObjectValue(String fieldName, Object target, Object source) {
//        throw new NotImplementedException("");
//        
//    }

    /** @return The {@link GenericDeserializer#deserializedObjects} field */
    public Map<String, Object> getDeserializedObjects() {
        return deserializedObjects;
    }

    /** @param deserializedObjects see the {@link GenericDeserializer#deserializedObjects} field */
    public void setDeserializedObjects(Map<String, Object> deserializedObjects) {
        this.deserializedObjects = deserializedObjects;
    }

}
