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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.NotImplementedException;

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
    
    private static final String CLASS_OR_INTERFACE_ARRAY_PREFIX = "L";
    private static final String ARRAY_OBJECT_SUFFIX = ";";
    private static final String ARRAY_LEFT_BRACKET = "\\[";
    private static final String START_ARRAY = "[";
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
        
        return deserializeObject(rootNode, null);
    }

    /** TODO Kyrylo Semenko */
    private Object deserializeObject(JsonNode currentNode, Class<?> defaultType) {
        try {
            Object instance = null;
            Class<?> clazz = null;
            String identifier = null;
            if (currentNode.has(GenericSerializer.GENERIC_KEY_ID)) {
                identifier = currentNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
                String className = identifier.substring(0, identifier.indexOf(GenericSerializer.GENERIC_VALUE_SEPARATOR));
                clazz = Class.forName(className);
                currentNode = currentNode.get(GenericSerializer.GENERIC_VALUE);
            } else {
                clazz = defaultType;
            }
            Object result = processPrimitiveOrWrapperOrString(currentNode, clazz);
            if (result != null) {
                return result;
            }
            instance = instantiate(clazz, currentNode);
            if (identifier != null) {
                deserializedObjects.put(identifier, instance);
            }
            
            deserializeSubFields(currentNode, instance, null);
            return instance;
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /** TODO */
    private Object instantiate(Class<?> clazz, JsonNode currentNode) throws InstantiationException, IllegalAccessException {
        try {
            if (clazz.getName().contains(START_ARRAY)) {
                return instantiateArray(clazz, currentNode);
            }
            return clazz.newInstance();
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
            if (jsonNode.has(GenericSerializer.GENERIC_KEY_ID)) {
                String a4id = jsonNode.get(GenericSerializer.GENERIC_KEY_ID).asText();
                if (deserializedObjects.containsKey(a4id)) {
                    // this node contains a reference to the previously deserialized object
                    Object value = deserializedObjects.get(a4id);
                    setToParent(parentInstance, parentFieldName, value);
                    return;
                }
            }
            deserializeFields(jsonNode, parentInstance, fieldNameList);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /** TODO Kyrylo Semenko */
    private void deserializeFields(JsonNode jsonNode, Object parentInstance, List<String> fieldNameList) {
        for (String fieldName : fieldNameList) {
            deserializeNode(jsonNode, parentInstance, fieldName);
        }
    }

    // TODO mozna spojit s deserializeObject
    private void deserializeNode(JsonNode jsonNode, Object parentInstance, String fieldName) {
        Class<?> clazz = null;
        try {
            JsonNode fieldNode = jsonNode.get(fieldName);
            
            if (!fieldNode.has(GenericSerializer.GENERIC_KEY_ID)) {
                deserializeNodeUsingReflection(fieldNode, parentInstance, fieldName);
                return;
            }
            
            JsonNode genericKeyId = fieldNode.get(GenericSerializer.GENERIC_KEY_ID);
            String genericKeyValue = genericKeyId.asText();
            if (deserializedObjects.containsKey(genericKeyValue)) {
                setToParent(parentInstance, fieldName, deserializedObjects.get(genericKeyValue));
                return;
            }
            String className = genericKeyValue.substring(0, genericKeyValue.indexOf(GenericSerializer.GENERIC_VALUE_SEPARATOR));
            clazz = Class.forName(className);
            fieldNode = fieldNode.get(GenericSerializer.GENERIC_VALUE);
            Object value = null;
            if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
                value = setPrimitiveOrWrapper(fieldNode, clazz);
            } else {
                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                if (containsEmptyConstructor(constructors)) {
                    value = clazz.newInstance();
                } else {
                    value = invokeNonemptyConstructor(fieldNode, parentInstance, fieldName, clazz);
                }
//                deserializeSubFields(fieldNode, value, fieldName);
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
    private Object invokeNonemptyConstructor(JsonNode fieldNode, Object parentInstance, String fieldName, Class<?> clazz) {
        try {
            Field field = parentInstance.getClass().getDeclaredField(fieldName);
            List<Object> parameters = new ArrayList<>();
            if (fieldNode.has(GenericSerializer.GENERIC_KEY_ID)) {
                JsonNode jsonNode = fieldNode.get(GenericSerializer.GENERIC_VALUE);
                Iterator<JsonNode> iterator = jsonNode.iterator();
                while (iterator.hasNext()) {
                    JsonNode nextNode = iterator.next();
                    Object nextObject = deserializeObject(nextNode, getContainerType(field));
                    parameters.add(nextObject);
                }
            } else {
                Iterator<JsonNode> iterator = fieldNode.iterator();
                while (iterator.hasNext()) {
                    JsonNode nextNode = iterator.next();
                    Object nextObject = deserializeObject(nextNode, getContainerType(field));
                    parameters.add(nextObject);
                }
            }
            
            if (clazz.isArray()) {
                return instantiateArray(clazz, fieldNode);
            }
            
            if (Collection.class.isAssignableFrom(clazz)) {
                return findAndInvokeConstructorOrMethod(field, parameters, clazz);
            }
            // Plain object
            return findAndInvokeConstructorOrMethod(field, parameters, clazz);
            
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    // TODO Auto-generated method stub
    private Object findAndInvokeConstructorOrMethod(Field field, List<Object> parameters, Class<?> clazz) {
        try {
            Type type = field.getGenericType();
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 1) {
                    throw new NotImplementedException("todo");
                }
                Constructor<?> constructor = findConstructor(field, clazz, type, actualTypeArguments, parameters);
                if (constructor != null) {
                    constructor.setAccessible(true);
                    return constructor.newInstance(parameters.toArray());
                }
                Method method = findStaticMethod(field, clazz, type, actualTypeArguments, parameters);
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

    // TODO Auto-generated method stub
    private Method findStaticMethod(Field field, Class<?> clazz, Type typeForReturning, Type[] actualTypeArguments, List<Object> parameters) {
        if (clazz == null) {
            return null;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (isMethodSuits(typeForReturning, parameters, method, actualTypeArguments)) {
                return method;
            }
        }
        for (Method method : field.getType().getDeclaredMethods()) {
            if (isMethodSuits(typeForReturning, parameters, method, actualTypeArguments)) {
                return method;
            }
        }
        Method result = findStaticMethod(field, clazz.getEnclosingClass(), typeForReturning, actualTypeArguments, parameters);
        if (result != null) {
            return result;
        }
        
        return null;
    }

    /** TODO */
    private boolean isMethodSuits(Type typeForReturning, List<Object> parameters, Method method, Type[] actualTypeArguments) {
        if (Modifier.isStatic(method.getModifiers())) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (isParametersSuit(parameterTypes, parameters) && typeForReturning == method.getReturnType()) {
                return true;
            }
            Class<?> methodParameterType = parameterTypes[0].getComponentType();
            if (method.isVarArgs() && isCollectionOrArray(typeForReturning) && methodParameterType.isAssignableFrom(parameters.get(0).getClass())) {
                return true; 
            }
        }
        return false;
    }

    // TODO Auto-generated method stub
    private Constructor<?> findConstructor(Field field, Class<?> clazz, Type typeForReturning, Type[] actualTypeArguments, List<Object> parameters) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (isConstructorSuit(typeForReturning, parameters, constructor)) {
                return constructor;
            }
        }
        for (Constructor<?> constructor : field.getType().getDeclaredConstructors()) {
            if (isConstructorSuit(typeForReturning, parameters, constructor)) {
                return constructor;
            }
        }
        return null;
    }

    /** TODO */
    private boolean isConstructorSuit(Type typeForReturning, List<Object> parameters, Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        return isParametersSuit(parameterTypes, parameters) ||
                (constructor.isVarArgs()
                && isCollectionOrArray(typeForReturning)
                && parameterTypes[0] == parameters.get(0).getClass());
    }

    // TODO Auto-generated method stub
    private boolean isCollectionOrArray(Type typeForReturning) {
        return typeForReturning.getClass().isArray() || ParameterizedType.class.isAssignableFrom(typeForReturning.getClass());
    }

    // TODO
    private boolean isParametersSuit(Class<?>[] parameterTypes, List<Object> parameters) {
        int length = parameterTypes.length;
        if (length != parameters.size()) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (parameterTypes[i] != parameters.get(i).getClass()) {
                return false;
            }
        }
        return true;
    }

    // TODO Auto-generated method stub
    private void findSuperClassesAndInterfaces(Class<?> clazz, ArrayList<Class<?>> superClassesAndInterfaces) {
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            superClassesAndInterfaces.add(superClass);
            findSuperClassesAndInterfaces(superClass, superClassesAndInterfaces);
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        superClassesAndInterfaces.addAll(Arrays.asList(interfaces));
        for (Class<?> nextInterface : interfaces) {
            findSuperClassesAndInterfaces(nextInterface, superClassesAndInterfaces);
        }
    }

    /** TODO Kyrylo Semenko stejny je v GenericSerializeru */
    private Class<? extends Object> getContainerType(Field field) {
        // Collections
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType parameterizedFieldType = (ParameterizedType) field.getGenericType();
            return (Class<?>) parameterizedFieldType.getActualTypeArguments()[0];
        }
        // Arrays or other objects
        return field.getType();
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
//            if (fieldType.isAssignableFrom(Collection.class)) {
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) fieldType.newInstance();
                int length = fieldNode.size();
                for (int i = 0; i < length; i++) {
                    JsonNode childJsonNode = fieldNode.get(i);
                    collection.add(deserializeObject(childJsonNode, getContainerType(field)));
                }
                setToParent(parentInstance, fieldName, collection);
                return;
            }
            
            // Complex objects
            Object value = deserializeObject(fieldNode, fieldType);
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
            } else if (arrayClassName.startsWith("Z")) {
                // TODO https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29
            } else if (arrayClassName.startsWith("B")) {
                // TODO https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29
            } else if (arrayClassName.startsWith("C")) {
                // TODO https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29
            } else if (arrayClassName.startsWith("D")) {
                // TODO https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29
            } else if (arrayClassName.startsWith("F")) {
                // TODO https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29
            } else if (arrayClassName.startsWith("I")) {
                componentType = Integer.TYPE;
            } else if (arrayClassName.startsWith("J")) {
                // TODO https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29
            } else if (arrayClassName.startsWith("S")) {
                // TODO https://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getName%28%29
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
                    Array.set(array, position++, deserializeObject(nextNode, arrayType));
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
