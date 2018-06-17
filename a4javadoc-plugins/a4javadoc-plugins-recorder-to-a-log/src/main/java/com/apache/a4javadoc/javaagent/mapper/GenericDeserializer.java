package com.apache.a4javadoc.javaagent.mapper;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import com.apache.a4javadoc.exception.AppRuntimeException;
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
     * see the {@link GenericSerializer#generateIdentifier(Object)} method.
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
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
        
        if (rootNode.get(GenericSerializer.GENERIC_KEY_ID) == null) {
            throw new AppRuntimeException("Root node without defined field with key '" + GenericSerializer.GENERIC_KEY_ID + "' is not accepted. RootNode: " + rootNode);
        }
        
        return deserializeObject(rootNode);
    }

    /**  */
    private Object deserializeObject(JsonNode currentNode) {
        try {
            Object instance = null;
            String identifier = currentNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
            String className = identifier.substring(0, identifier.indexOf(GenericSerializer.GENERIC_VALUE_SEPARATOR));
            Class<?> clazz = Class.forName(className);
            Object result = processPrimitiveOrWrapperOrString(currentNode, clazz);
            if (result != null) {
                return result;
            }
            instance = clazz.newInstance();
            deserializedObjects.put(identifier, instance);
            
            deserializeSubFields(currentNode, instance, null);
            return instance;
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**  */
    private Object processPrimitiveOrWrapperOrString(JsonNode currentNode, Class<?> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (clazz.equals(String.class) || ClassUtils.isPrimitiveOrWrapper(clazz)) {
            return setPrimitiveOrWrapper(currentNode, clazz);
        }
        return null;
    }

    /**  */
    private Object setPrimitiveOrWrapper(JsonNode rootNode, Class<?> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
//        String jsonValue = rootNode.get(GenericSerializer.PRIMITIVE_OR_WRAPPER_VALUE).textValue();
//        Method valueOf = clazz.getDeclaredMethod("valueOf", String.class);
//        Object instance = clazz.newInstance();
//        valueOf.invoke(instance, jsonValue);
//        return instance;
        
      String jsonValue = rootNode.get(GenericSerializer.PRIMITIVE_OR_WRAPPER_VALUE).textValue();
      Constructor<?> constructor = clazz.getConstructor(String.class);
      return constructor.newInstance(jsonValue);
    }
    
    /**  
     * This method calls the {@link #deserializeFields(JsonNode, Object, List)} method, which calls this method recursively.<br>
     * At first check if the jsonNode contains a single value.
     * If so, the value is a reference to already deserialized object from {@link #deserializedObjects}. Set it to instance and return.
     * Else call the {@link #deserializeFields(JsonNode, Object, List)} method. 
     */
    private void deserializeSubFields(JsonNode jsonNode, Object parentInstance, String parentFieldName) {
        try {
            Iterator<String> fieldNames = jsonNode.fieldNames();
            List<String> fieldNameList = new ArrayList<>();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                if (GenericSerializer.GENERIC_KEY_ID.equals(fieldName)) {
                    continue;
                }
                fieldNameList.add(fieldName);
            }
            if (fieldNameList.isEmpty()) {
                // this node contains a reference to the previously deserialized object
                String a4id = jsonNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
                Object value = deserializedObjects.get(a4id);
                setToParent(parentInstance, parentFieldName, value);
                return;
            }
            deserializeFields(jsonNode, parentInstance, fieldNameList);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /** TODO Kyrylo Semenko */
    private void deserializeFields(JsonNode jsonNode, Object parentInstance, List<String> fieldNameList) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        for (String fieldName : fieldNameList) {
            JsonNode fieldNode = jsonNode.get(fieldName);
            if (jsonNode.get(fieldName).isNull()) {
                setToParent(parentInstance, fieldName, null);
                continue;
            }
            JsonNode genericKeyId = fieldNode.get(GenericSerializer.GENERIC_KEY_ID);
            if (genericKeyId == null) {
                setToParent(parentInstance, fieldName, fieldNode.asText());
                continue;
//                throw new AppRuntimeException("JsonNode without defined field with key '" + GenericSerializer.GENERIC_KEY_ID + "' is not accepted. RootNode: " + fieldNode);
            }
            String genericKeyValue = genericKeyId.asText();
            if (deserializedObjects.containsKey(genericKeyValue)) {
                setToParent(parentInstance, fieldName, deserializedObjects.get(genericKeyValue));
                continue;
            }
            String className = genericKeyValue.substring(0, genericKeyValue.indexOf(GenericSerializer.GENERIC_VALUE_SEPARATOR));
            Class<?> clazz = Class.forName(className);
            Object value = null;
            if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
                value = setPrimitiveOrWrapper(fieldNode, clazz);
            } else {
                try {
                    value = clazz.newInstance();
                } catch (Exception e) {
                    throw new AppRuntimeException("Cannot instantiate class: "
                            + clazz.getCanonicalName()
                            + ", field for deserialization: "
                            + fieldName
                            + ", parentInstance: "
                            + parentInstance.getClass().getCanonicalName(), e);
                }
                deserializeSubFields(fieldNode, value, fieldName);
            }
            setToParent(parentInstance, fieldName, value);
        }
    }

    /**
     * For each method of the instance call the {@link #tryToInvokeBySetter(Object, String, Object, Method)} method.<br>
     * If result is 'false', call the {@link Field#set(Object, Object)} method.
     */
    private void setToParent(Object parentInstance, String fieldName, Object value) {
        try {
            for (Method method : parentInstance.getClass().getMethods()) {
                boolean invokedSuccessfully = tryToInvokeBySetter(parentInstance, fieldName, value, method);
                if (invokedSuccessfully) {
                    return;
                }
            }
            Field field = parentInstance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(parentInstance, value);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * If the method the field setter, try to invoke the setter on the instance.
     * @param instance some class instance where to set the value
     * @param fieldName the {@link Field} name
     * @param value field value to set on the instance
     * @param method invocation candidate
     */
    private boolean tryToInvokeBySetter(Object instance, String fieldName, Object value, Method method) {
        if (method.getParameterTypes().length == 1
                && method.getName().startsWith("set")
                && method.getName().length() == (fieldName.length() + 3)
                && method.getName().toLowerCase().endsWith(fieldName.toLowerCase())) {
            
            try {
                Class<?> type = instance.getClass().getDeclaredField(fieldName).getType();
                if (type.equals(String.class) || ClassUtils.isPrimitiveOrWrapper(type)) {
                    if (ClassUtils.isPrimitiveWrapper(type) || type.equals(String.class)) {
                        Constructor<?> constructor = type.getConstructor(String.class);
                        Object object = constructor.newInstance((String) value);
                        method.invoke(instance, object);
                    } else {
                        // primitive
                        Object object = toObject(type, (String)value);
                        method.invoke(instance, object);
                    }
                } else {
                    method.invoke(instance, value);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    private static Object toObject( Class<?> type, String value ) {
        if (Boolean.TYPE == type) {
            return Boolean.parseBoolean(value);
        }
        
        if (Byte.TYPE == type) {
            return Byte.parseByte(value);
        }
        
        if (Short.TYPE == type) {
            return Short.parseShort(value);
        }
        
        if (Integer.TYPE == type) {
            return Integer.parseInt(value);
        }
        
        if (Long.TYPE == type) {
            return Long.parseLong(value);
        }
        
        if (Float.TYPE == type) {
            return Float.parseFloat(value);
        }
        
        if (Double.TYPE == type) {
            return Double.parseDouble(value);
        }
        
        throw new AppRuntimeException("Cannot parse value: '" + value + "' of type: " + type);
    }

    /** @return The {@link GenericDeserializer#deserializedObjects} field */
    public Map<String, Object> getDeserializedObjects() {
        return deserializedObjects;
    }

    /** @param deserializedObjects see the {@link GenericDeserializer#deserializedObjects} field */
    public void setDeserializedObjects(Map<String, Object> deserializedObjects) {
        this.deserializedObjects = deserializedObjects;
    }

}
