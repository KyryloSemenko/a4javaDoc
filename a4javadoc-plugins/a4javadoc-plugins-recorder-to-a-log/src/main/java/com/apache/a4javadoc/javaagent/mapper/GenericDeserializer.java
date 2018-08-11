package com.apache.a4javadoc.javaagent.mapper;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;

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
    
    /** See <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29">Class javaDoc</a> */
    private static final String TYPE_ENCODING_PRIMITIVE_SHORT_S = "S";
    
    /** See <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29">Class javaDoc</a> */
    private static final String TYPE_ENCODING_PRIMITIVE_LONG_J = "J";
    
    /** See <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29">Class javaDoc</a> */
    private static final String TYPE_ENCODING_PRIMITIVE_INT_I = "I";
    
    /** See <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29">Class javaDoc</a> */
    private static final String TYPE_ENCODING_PRIMITIVE_FLOAT_F = "F";
    
    /** See <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29">Class javaDoc</a> */
    private static final String TYPE_ENCODING_PRIMITIVE_DOUBLE_D = "D";
    
    /** See <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29">Class javaDoc</a> */
    private static final String TYPE_ENCODING_PRIMITIVE_CHAR_C = "C";
    
    /** See <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29">Class javaDoc</a> */
    private static final String TYPE_ENCODING_PRIMITIVE_BYTE_B = "B";
    
    /** See <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29">Class javaDoc</a> */
    private static final String TYPE_ENCODING_PRIMITIVE_BOOLEAN_Z = "Z";
    
    /** See <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29">Class javaDoc</a> */
    private static final String CLASS_OR_INTERFACE_ARRAY_PREFIX = "L";
    
    private static final String ARRAY_OBJECT_SUFFIX = ";";
    private static final String ARRAY_LEFT_BRACKET = "\\[";
    private static final String START_ARRAY = "[";
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
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
        
        if (rootNode.get(GenericSerializer.GENERIC_KEY_ID) == null) {
            throw new AppRuntimeException("Root node without defined field with key '" + GenericSerializer.GENERIC_KEY_ID + "' is not accepted. Is this JSON string serialized by '" + GenericSerializer.class.getCanonicalName() + "'?. RootNode: " + rootNode);
        }
        
        return deserializeObject(rootNode, null, null, null, null, null);
    }

    /** TODO Kyrylo Semenko */
    private Object deserializeObject(JsonNode currentNode, Class<?> defaultType, List<Class<?>> containerTypes, String defaultIdentifier, Object parentInstance, String fieldName) {
        try {
            Object instance = null;
            Class<?> clazz = null;
            String identifier = defaultIdentifier;
            if (currentNode.has(GenericSerializer.GENERIC_KEY_ID)) {
                identifier = currentNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
                if (deserializedObjects.containsKey(identifier)) {
                    return deserializedObjects.get(identifier);
                }
                String className = IdentifierService.getInstance().findClassName(identifier);
                clazz = Class.forName(className);
                currentNode = currentNode.get(GenericSerializer.GENERIC_VALUE);
            } else {
                clazz = defaultType;
            }
            Object result = processPrimitiveOrWrapperOrString(currentNode, clazz);
            if (result != null) {
                return result;
            }
            instance = instantiate(clazz, containerTypes, currentNode, parentInstance, fieldName, identifier);
            if (identifier != null) {
                deserializedObjects.put(identifier, instance);
            }
            
            deserializeSubFields(currentNode, instance, null, identifier);
            return instance;
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /** TODO  
     * @param containerTypes */
    private Object instantiate(Class<?> classForInstantiation, List<Class<?>> containerTypes, JsonNode currentNode, Object parentInstance, String fieldName, String identifier) {
        try {
            if (classForInstantiation.getName().contains(START_ARRAY)) {
                return instantiateArray(classForInstantiation, currentNode);
            }
            if (containsEmptyConstructor(classForInstantiation.getDeclaredConstructors())) {
                return classForInstantiation.newInstance();
            }
            return invokeFactoryOrNonemptyConstructor(currentNode, parentInstance, fieldName, classForInstantiation, identifier);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    // TODO Auto-generated method stub
    private void fillDimensionSizes(JsonNode currentNode, int currentDimension, int[] dimensionSizes) {
        dimensionSizes[currentDimension] = currentNode.size();
        if (dimensionSizes.length == currentDimension + 1) {
            return;
        }
        fillDimensionSizes(currentNode.get(0), currentDimension + 1, dimensionSizes);
    }

    /** TODO */
    private Object processPrimitiveOrWrapperOrString(JsonNode currentNode, Class<?> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (clazz.equals(String.class) || ClassUtils.isPrimitiveOrWrapper(clazz)) {
            return setPrimitiveOrWrapper(currentNode, clazz);
        }
        return null;
    }

    /** TODO */
    private Object setPrimitiveOrWrapper(JsonNode rootNode, Class<?> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String jsonValue = null;
        if (rootNode.has(GenericSerializer.GENERIC_KEY_ID)) {
            jsonValue = rootNode.get(GenericSerializer.GENERIC_VALUE).textValue();
        } else {
            jsonValue = rootNode.textValue();
        }
        return toObject(clazz, jsonValue);
    }
    
    /**  
     * This method call the {@link #deserializeFields(JsonNode, Object, List)} method, which calls this method recursively.<br>
     * At first check if the jsonNode contains a single value. TODO Kyrylo Semenko.
     * If so, the value is a reference to already deserialized object from {@link #deserializedObjects}. Set it to instance and return.
     * Else call the {@link #deserializeFields(JsonNode, Object, List)} method. 
     */
    private void deserializeSubFields(JsonNode jsonNode, Object parentInstance, String parentFieldName, String parentIdentifier) {
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
            if (jsonNode.has(GenericSerializer.GENERIC_KEY_ID)) {
                String a4id = jsonNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
                if (deserializedObjects.containsKey(a4id)) {
                    // this node contains a reference to the previously deserialized object
                    Object value = deserializedObjects.get(a4id);
                    setToParent(parentInstance, parentFieldName, value);
                    return;
                }
            }
            deserializeFields(jsonNode, parentInstance, fieldNameList, parentIdentifier);
            deserializeMap(jsonNode, parentInstance, parentIdentifier);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    // TODO Auto-generated method stub
    private boolean deserializeMap(JsonNode jsonNode, Object mapInstance, String parentIdentifier) {
        if (Map.class.isAssignableFrom(mapInstance.getClass())) {
            Class<?> keyClass = IdentifierService.getInstance().findKeyClass(parentIdentifier);
            Class<?> valueClass = IdentifierService.getInstance().findValueClass(parentIdentifier);
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) mapInstance;
            Iterator<JsonNode> iterator = jsonNode.iterator();
            while (iterator.hasNext()) {
                JsonNode nextNode = iterator.next();
                Object keyObject = deserializeObject(nextNode.get(0), keyClass, null, null, mapInstance, null);
                Object valueObject = deserializeObject(nextNode.get(1), valueClass, null, null, mapInstance, null);
                map.put(keyObject, valueObject);
            }
            return true;
        }
        return false;
    }

    /** TODO Kyrylo Semenko */
    private void deserializeFields(JsonNode jsonNode, Object parentInstance, List<String> fieldNameList, String parentIdentifier) {
        for (String fieldName : fieldNameList) {
            deserializeNode(jsonNode, parentInstance, fieldName, parentIdentifier);
        }
    }

    // TODO mozna spojit s deserializeObject
    private void deserializeNode(JsonNode jsonNode, Object parentInstance, String fieldName, String parentIdentifier) {
        Class<?> clazz = null;
        try {
            JsonNode fieldNode = jsonNode.get(fieldName);
            
            if (!fieldNode.has(GenericSerializer.GENERIC_KEY_ID)) {
                deserializeNodeUsingReflection(fieldNode, parentInstance, fieldName);
                return;
            }
            
            JsonNode genericKeyId = fieldNode.get(GenericSerializer.GENERIC_KEY_ID);
            String identifier = genericKeyId.asText();
            if (deserializedObjects.containsKey(identifier)) {
                setToParent(parentInstance, fieldName, deserializedObjects.get(identifier));
                return;
            }
            String className = IdentifierService.getInstance().findClassName(identifier);
            clazz = Class.forName(className);
            JsonNode valueFieldNode = fieldNode.get(GenericSerializer.GENERIC_VALUE);
            Object value = null;
            if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
                value = setPrimitiveOrWrapper(valueFieldNode, clazz);
            } else {
                value = deserializeObject(valueFieldNode, clazz, null, identifier, parentInstance, fieldName);
            }
            setToParent(parentInstance, fieldName, value);
        } catch (Exception e) {
            throw new AppRuntimeException("Cannot instantiate class: '"
                    + clazz
                    + "', field for deserialization: "
                    + fieldName
                    + ", parentInstance: "
                    + parentInstance.getClass().getCanonicalName(), e);
        }
    }

    // TODO Auto-generated method stub
    // Create an empty instance
    private Object invokeFactoryOrNonemptyConstructor(JsonNode jsonNode, Object parentInstance, String fieldName, Class<?> clazz, String identifier) {
        try {
            Field field = null;
            if (parentInstance != null && fieldName != null) {
                field = parentInstance.getClass().getDeclaredField(fieldName);
            }
            List<Object> parameters = new ArrayList<>();
            List<Class<?>> containerTypes = FieldService.getInstance().getContainerTypes(field, null, identifier);
            if (jsonNode.has(GenericSerializer.GENERIC_KEY_ID)) {
                JsonNode valueNode = jsonNode.get(GenericSerializer.GENERIC_VALUE);
                Iterator<JsonNode> iterator = valueNode.iterator();
                while (iterator.hasNext()) {
                    JsonNode nextNode = iterator.next();
                    Object nextObject = deserializeObject(
                            nextNode,
                            null,
                            containerTypes,
                            jsonNode.get(GenericSerializer.GENERIC_KEY_ID).asText(),
                            parentInstance,
                            fieldName);
                    parameters.add(nextObject);
                }
            } else {
                Iterator<JsonNode> iterator = jsonNode.iterator();
                while (iterator.hasNext()) {
                    JsonNode nextNode = iterator.next();
                    Object nextObject = deserializeObject(
                            nextNode,
                            null,
                            containerTypes,
                            identifier,
                            parentInstance,
                            fieldName);
                    parameters.add(nextObject);
                }
            }
            
            if (clazz.isArray()) {
                return instantiateArray(clazz, jsonNode);
            }
            
            if (Collection.class.isAssignableFrom(clazz)) {
                return findAndInvokeConstructorOrMethod(field, parameters, clazz, identifier);
            }
            // Plain object
            return findAndInvokeConstructorOrMethod(field, parameters, clazz, identifier);
            
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    // TODO Auto-generated method stub
    private Object findAndInvokeConstructorOrMethod(Field field, List<Object> parameters, Class<?> clazz, String identifier) {
        try {
            Class<?> fieldType = null;
            if (identifier != null) {
                fieldType = IdentifierService.getInstance().findClass(identifier);
            } else if (field != null) {
                // in this place primitive types can not occur
                fieldType = (Class<?>) field.getGenericType();
            } else {
                fieldType = clazz;
            }
            Constructor<?> constructor = ConstructorService.getInstance().findConstructor(field, clazz, fieldType, parameters);
            if (constructor != null) {
                constructor.setAccessible(true);
                return constructor.newInstance(parameters.toArray());
            }
            if (field != null) {
                Method method = findStaticMethod(field.getGenericType(), clazz, parameters, field.getType().getDeclaredMethods());
                if (method != null) {
                    method.setAccessible(true);
                    return method.invoke(null, (Object) parameters.toArray());
                }
            } else {
                Method method = findStaticMethod(clazz, clazz, parameters, clazz.getDeclaredMethods());
                if (method != null) {
                    method.setAccessible(true);
                    return method.invoke(null, (Object) parameters.toArray());
                }
            }
            throw new AppRuntimeException("Cannot instantiate the object");
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

//    /**
//     * This method is recursive. It finds out a static factory for instantiation of a field object.
//     * @param field class field with object to be instantiated
//     * @param clazz concrete type of objects which this field will be contain
//     * @param parameters instances of objects which this field will be contain
//     */
//    private Method findStaticMethod(Field field, Class<?> clazz, List<Object> parameters) {
//        if (clazz == null) {
//            return null;
//        }
//        Type typeForReturning = field.getGenericType();
//        for (Method method : clazz.getDeclaredMethods()) {
//            if (isMethodSuits(typeForReturning, parameters, method)) {
//                return method;
//            }
//        }
//        for (Method method : field.getType().getDeclaredMethods()) {
//            if (isMethodSuits(typeForReturning, parameters, method)) {
//                return method;
//            }
//        }
//        Method result = findStaticMethod(field, clazz.getEnclosingClass(), parameters);
//        if (result != null) {
//            return result;
//        }
//        
//        return null;
//    }
    
    /**
     * This method is recursive. It finds out a static factory for instantiation of a field object.
     * @param typeForReturning future instance type
     * @param clazz concrete type of objects which this field will be contain
     * @param parameters instances of objects which this field will be contain
     */
    private Method findStaticMethod(Type typeForReturning, Class<?> clazz, List<Object> parameters, Method[] declaredMethods) {
        if (clazz == null) {
            return null;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (isMethodSuits(typeForReturning, parameters, method)) {
                return method;
            }
        }
        for (Method method : declaredMethods) {
            if (isMethodSuits(typeForReturning, parameters, method)) {
                return method;
            }
        }
        Method result = findStaticMethod(typeForReturning, clazz.getEnclosingClass(), parameters, declaredMethods);
        if (result != null) {
            return result;
        }
        
        return null;
    }

    /** TODO */
    private boolean isMethodSuits(Type typeForReturning, List<Object> parameters, Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (ParameterService.getInstance().isParametersSuit(parameterTypes, parameters, method.isVarArgs())
                    && TypeService.getInstance().isClassAssignableFromType(method.getReturnType(), typeForReturning)) {
                return true;
            }
//            Class<?> methodParameterType = parameterTypes[0].getComponentType();
//            if (method.isVarArgs()
//                    && TypeService.getInstance().isCollectionOrArray(typeForReturning)
//                    && methodParameterType.isAssignableFrom(parameters.get(0).getClass())) {
//                return true; 
//            }
        }
        return false;
    }

    // TODO Auto-generated method stub
    private boolean containsEmptyConstructor(Constructor<?>[] constructors) {
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }

    // TODO Auto-generated method stub
    private void deserializeNodeUsingReflection(JsonNode fieldNode, Object parentInstance, String fieldName) {
        try {
            // null
            if (fieldNode.isNull()) {
                setToParent(parentInstance, fieldName, null);
                return;
            }
            
            Field field = parentInstance.getClass().getDeclaredField(fieldName);
            Class<?> fieldType = field.getType();
            
            // String, primitive or wrapper
            if (ClassUtils.isPrimitiveOrWrapper(fieldType) || String.class == fieldType) {
                setToParent(parentInstance, fieldName, fieldNode.asText());
                return;
            }
            
            // Array
            if (fieldType.isArray()) {
                Object array = instantiateArray(fieldType, fieldNode);
                setToParent(parentInstance, fieldName, array);
                return;
            }
            
            // Collection
            if (Collection.class.isAssignableFrom(fieldType)) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) fieldType.newInstance();
                int length = fieldNode.size();
                for (int i = 0; i < length; i++) {
                    JsonNode childJsonNode = fieldNode.get(i);
                    collection.add(deserializeObject(
                            childJsonNode,
                            null,
                            FieldService.getInstance().getContainerTypes(field, null, null),
                            null,
                            parentInstance,
                            fieldName));
                }
                setToParent(parentInstance, fieldName, collection);
                return;
            }
            
            // Complex objects
            Object value = deserializeObject(fieldNode, fieldType, null, null, parentInstance, fieldName);
            setToParent(parentInstance, fieldName, value);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
        
    }

    /** TODO */
    private Object instantiateArray(Class<?> clazz, JsonNode jsonNode) {
        try {
            int beginIndex = clazz.getName().lastIndexOf(START_ARRAY) + START_ARRAY.length();
            int endIndex = clazz.getName().length();
            if (clazz.getName().contains(ARRAY_OBJECT_SUFFIX)) {
                endIndex = clazz.getName().indexOf(ARRAY_OBJECT_SUFFIX);
            }
            String arrayClassName = clazz.getName().substring(beginIndex, endIndex);
            Class<?> componentType = null;
            if (arrayClassName.startsWith(CLASS_OR_INTERFACE_ARRAY_PREFIX)) {
                componentType = Class.forName(arrayClassName.substring(1));
            } else if (arrayClassName.startsWith(TYPE_ENCODING_PRIMITIVE_BOOLEAN_Z)) {
                componentType = boolean.class;
            } else if (arrayClassName.startsWith(TYPE_ENCODING_PRIMITIVE_BYTE_B)) {
                componentType = byte.class;
            } else if (arrayClassName.startsWith(TYPE_ENCODING_PRIMITIVE_CHAR_C)) {
                componentType = char.class;
            } else if (arrayClassName.startsWith(TYPE_ENCODING_PRIMITIVE_DOUBLE_D)) {
                componentType = double.class;
            } else if (arrayClassName.startsWith(TYPE_ENCODING_PRIMITIVE_FLOAT_F)) {
                componentType = float.class;
            } else if (arrayClassName.startsWith(TYPE_ENCODING_PRIMITIVE_INT_I)) {
                componentType = int.class;
            } else if (arrayClassName.startsWith(TYPE_ENCODING_PRIMITIVE_LONG_J)) {
                componentType = long.class;
            } else if (arrayClassName.startsWith(TYPE_ENCODING_PRIMITIVE_SHORT_S)) {
                componentType = short.class;
            }
            int dimensionsNumber = clazz.getName().split(ARRAY_LEFT_BRACKET).length - 1;
            int[] dimensionSizes = new int[dimensionsNumber];
            fillDimensionSizes(jsonNode, 0, dimensionSizes);
            Object array = Array.newInstance(componentType, dimensionSizes);
            fillArray(componentType, jsonNode, 0, dimensionSizes, array);
            return array;
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    // TODO Auto-generated method stub
    @SuppressWarnings("unchecked")
    private <T> void fillArray(Class<T> arrayType, JsonNode jsonNode, int currentDimensionNumber, int[] dimensionSizes, Object array) {
        Iterator<JsonNode> iterator = jsonNode.iterator();
        int position = 0;
        while (iterator.hasNext()) {
            JsonNode nextNode = iterator.next();
            if (currentDimensionNumber < dimensionSizes.length - 1) {
                T[] subArray = (T[]) Array.get(array, position++);
                fillArray(arrayType, nextNode, currentDimensionNumber + 1, dimensionSizes, subArray);
            } else {
                if (nextNode.isNull()) {
                    Array.set(array, position++, null);
                } else {
                    Array.set(array, position++, deserializeObject(nextNode, arrayType, null, null, null, null));
                }
            }
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
    
    private Object toObject(Class<?> type, String value) {
        if (Boolean.TYPE == type || Boolean.class == type) {
            return Boolean.parseBoolean(value);
        }
        
        if (Byte.TYPE == type || Byte.class == type) {
            return Byte.parseByte(value);
        }
        
        if (Short.TYPE == type || Short.class == type) {
            return Short.parseShort(value);
        }
        
        if (Integer.TYPE == type || Integer.class == type) {
            return Integer.parseInt(value);
        }
        
        if (Long.TYPE == type || Long.class == type) {
            return Long.parseLong(value);
        }
        
        if (Float.TYPE == type || Float.class == type) {
            return Float.parseFloat(value);
        }
        
        if (Double.TYPE == type || Double.class == type) {
            return Double.parseDouble(value);
        }
        
        if (String.class == type) {
            return value;
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
    
    // TODO vyhodit jackson, protoze je zbytecne vytvaret NodeFactory a dalsi veci.

}
