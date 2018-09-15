package com.apache.a4javadoc.javaagent.mapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Stateless singleton for working objects identifier.
 * Example of an identifier:
 * 
 * <pre>
 * "_a4id": {
 *     "hash": "43f19bbe",
 *     "containerType": {
 *         "objectClass": "com.apache.a4javadoc.javaagent.mapper.WrapperClass",
 *         "containerTypes": []
 *     }
 * }
 * </pre>
 * 
 * @author Kyrylo Semenko
 */
public class IdentifierService {

    private static final String CONTAINER_TYPES_FIELD_NAME = "containerTypes";

    private static final String OBJECT_CLASS_FIELD_NAME = "objectClass";

    private static final String CONTAINER_TYPE_FIELD_NAME = "containerType";

    /**
     * A left bracket for example in the
     * 
     * <pre>
     * {@code "ArrayList<java.lang.String>}@fe2"
     * </pre>
     * 
     * identifier
     **/
    public static final String GENERIC_LEFT_BRACKET = "<";

    /**
     * A right bracket for example in the
     * 
     * <pre>
     * {@code "ArrayList<java.lang.String>}@fe2"
     * </pre>
     * 
     * identifier
     **/
    public static final String GENERIC_RIGHT_BRACKET = ">";

    /**
     * Separator of class name and hash in for example
     * 
     * <pre>
     * "com.apache.a4javadoc.javaagent.mapper.CircularClass@2d332eab"
     * </pre>
     */
    public static final String GENERIC_VALUE_SEPARATOR = "@";

    /**
     * A comma between generic types, for example in the
     * 
     * <pre>
     * {@code "java.util.TreeMap<java.lang.Integer,java.lang.String>@370b5"}
     * </pre>
     * 
     * identifier
     **/
    public static final String GENERIC_COMMA = ",";

    private static final String EMPTY_STRING = "";

    private static IdentifierService instance;

    private IdentifierService() {
        // empty
    }

    /**
     * @return the {@link IdentifierService} singleton.
     */
    public static IdentifierService getInstance() {
        if (instance == null) {
            instance = new IdentifierService();
        }
        return instance;
    }

    /**
     * Generate an identifier from the method argument
     * and generic types if exist
     * and {@link Object#hashCode()} in hexadecimal form,
     * for example
     * 
     * <pre>
            "_a4id": {
                "hash": "43f19bbe",
                "containerType": {
                    "objectClass": "com.apache.a4javadoc.javaagent.mapper.WrapperClass",
                    "containerTypes": []
                }
            }
     * </pre>
     * 
     * for non - generic object, or
     * 
     * <pre>
            "_a4id": {
                "hash": "319aabc3",
                "containerType": {
                    "objectClass": "java.util.TreeMap",
                    "containerTypes": [{
                        "objectClass": "com.apache.a4javadoc.javaagent.mapper.Container",
                        "containerTypes": []
                    },
                    {
                        "objectClass": "com.apache.a4javadoc.javaagent.mapper.Container",
                        "containerTypes": []
                    }]
                }
            }
     * </pre>
     * 
     * for a {@link Map}, or more complex
     * 
     * <pre>
            "_a4id": {
                "hash": "27df",
                "containerType": {
                    "objectClass": "java.util.LinkedHashMap",
                    "containerTypes": [{
                        "objectClass": "java.lang.String",
                        "containerTypes": []
                    },
                    {
                        "objectClass": "java.util.LinkedHashMap",
                        "containerTypes": [{
                            "objectClass": "java.lang.String",
                            "containerTypes": []
                        },
                        {
                            "objectClass": "java.lang.Integer",
                            "containerTypes": []
                        }]
                    }]
                }
            }
     * </pre>
     * 
     * for a {@link LinkedHashMap} with {@link String} as a key and
     * {@link LinkedHashMap} as a value.
     * 
     * @param value the identifier source
     * @return the identifier of the 'value' argument
     */
    public Identifier createIdentifier(Object value) {
        Identifier identifier = new Identifier();

        ContainerType containerType = new ContainerType();
        identifier.setContainerType(containerType);

        setContainerTypes(value, containerType, 1);

        return identifier;
    }

    /**
     * @deprecated
     */
    public String generateIdentifier(Object value) {
        return value.getClass().getName() + generateGenericSignature(value) + GENERIC_VALUE_SEPARATOR
                + Integer.toHexString(value.hashCode());
    }

    /**
     * Find out generic types of the value, for example
     * {@code <java.lang.String,java.lang.String>}.
     * 
     * @param value object instance as a source of the generic types. If the
     * value is 'null', it will be skipped.
     * @param containerType this object will be completed by generic types, see
     * {@link ContainerType#setContainerTypes(List)}.
     * @param depth Plunging depth of this {@link ContainerType}, beginning from
     * 1. Max depth defined in {@link ConfigService#getMaxDepth()}.
     */
    public void setContainerTypes(Object value, ContainerType containerType, int depth) {
        try {
            if (value == null || depth > ConfigService.getInstance().getMaxDepth()) {
                return;
            }
            Class<?> clazz = value.getClass();
            containerType.setObjectClass(value.getClass());
            if (clazz.isArray()) {
                findGeneralItemsTypeOfArray(value, containerType, depth);
                return;
            }
            if (Iterable.class.isAssignableFrom(clazz)) {
                Iterable<?> iterable = (Iterable<?>) value;
                findGeneralItemsTypeOfIterable(iterable, containerType, depth);
                return;
            }
            if (Map.class.isAssignableFrom(clazz)) {
                Map<?, ?> map = (Map<?, ?>) value;
                findGeneralItemsTypeOfMap(map, containerType, depth);
                return;
            }
            if (ParameterizedType.class.isAssignableFrom(clazz.getGenericSuperclass().getClass())) {
                Type[] types = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
                List<TypeVariable<?>> typeVariables = new ArrayList<>();
                for (Type type : types) {
                    if (type instanceof TypeVariable<?>) {
                        @SuppressWarnings("unchecked")
                        TypeVariable<Class<?>> typeVariable = (TypeVariable<Class<?>>) type;
                        typeVariables.add(typeVariable);
                    } else {
                        throw new AppRuntimeException("Type should be TypeVariable. Type: " + type);
                    }
                }
                if (types.length > 0) {
                    List<Class<?>> generalItemTypes = findGeneralItemTypes(value, typeVariables, containerType, depth);
    //                Method factoryMethod = MethodService.getInstance().findFactoryMethod(value, types);
    //                if (factoryMethod != null) {
    //                    Method iteratorMethod = MethodService.getInstance().findIteratorMethod(value);
    //                    if (iteratorMethod != null) {
    //                        Iterable<Class<?>> iterable = (Iterable<Class<?>>) iteratorMethod.invoke(value);
    //                        findGeneralItemsTypeOfIterable(iterable, containerType, depth);
    //                    } else {
    //                        Method mapMethod = MethodService.getInstance().getMapMethod(value);
    //                    }
    //                    Method itemFactoryMethod = MethodService.getInstance().findItemFactoryMethod(value, types);
    //                    if (itemFactoryMethod != null) {
    //                        
    //                        containerType.setFactoryMethod(factoryMethod);
    //                        containerType.setItemFactoryMethod(itemFactoryMethod);
    //                    }
    //                }
                    
                    
    //                Constructor constructor = ConstructorService.getInstance().findConstructor(value, types);
    //                if (constructor != null) {
    //                    Object copyOfValue = constructor.newInstance();
    //                }
                    // TODO Kyrylo Semenko - dohledat factory method a itemFactory
                    // method.
                }
            }
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * Find out iterator method of the value, iterate items and store
     * theirs general boundary types in the {@link ContainerType}.
     * 
     * @param value the object that contains {@link Set} of items
     * @param typeVariables contains boundaries of elements in a single item
     * @param containerType container to story founded information
     * @param depth the current plunge depth of this {@link ContainerType}
     * @return 'null' if cannot find out an iterable method. 
     * @throws Exception 
     */
    private List<Class<?>> findGeneralItemTypes(Object value, List<TypeVariable<?>> typeVariables, ContainerType containerType, int depth) throws Exception {
        Method iterableMethod = MethodService.getInstance().findIterableOrMapMethod(value, typeVariables);
        if (iterableMethod == null) {
            return null;
        }
        Iterable<?> iterable = (Iterable<?>) iterableMethod.invoke(value);
        findGeneralItemsTypeOfIterable(iterable, containerType, depth);
        throw new NotImplementedException("not implemented yet");
    }

    /**
     * Find out the most general type of the {@link Array} values.
     * 
     * @param array the container of objects. This method will iterate these
     * objects and find out theirs general type.
     * @param containerType This object will be completed by found general type,
     * se the {@link ContainerType#getContainerTypes()} method.
     * @param depth plunge depth of this {@link ContainerType}, beginning from 1
     */
    private void findGeneralItemsTypeOfArray(Object array, ContainerType containerType, int depth) {
        ContainerType commonContainerType = new ContainerType();
        for (int i = 0; i < Array.getLength(array); i++) {
            Object object = Array.get(array, i);
            ContainerType currentContainerType = new ContainerType();

            setContainerTypes(object, currentContainerType, depth + 1);
            mergeToCommonContainer(currentContainerType, commonContainerType);
        }
        if (commonContainerType.getObjectClass() != null) {
            containerType.getContainerTypes().add(commonContainerType);
        }
    }

    /**
     * Find out the most general type of the {@link Iterable} values.
     * 
     * @param iterable the container of objects. This method will iterate these
     * objects and find out theirs general type.
     * @param containerType This object will be completed by found general type,
     * se the {@link ContainerType#getContainerTypes()} method.
     * @param depth Depth of plunge this {@link ContainerType}, beginning from 1
     * If the inner items are generic, return for example
     * {@code <java.util.ArrayList<java.lang.String>>}
     */
    private void findGeneralItemsTypeOfIterable(Iterable<?> iterable, ContainerType containerType, int depth) {
        ContainerType commonContainerType = new ContainerType();
        Iterator<?> valuesIterator = iterable.iterator();
        while (valuesIterator.hasNext()) {
            Object object = valuesIterator.next();
            ContainerType currentContainerType = new ContainerType();

            setContainerTypes(object, currentContainerType, depth + 1);
            mergeToCommonContainer(currentContainerType, commonContainerType);
        }
        if (commonContainerType.getObjectClass() != null) {
            containerType.getContainerTypes().add(commonContainerType);
        }
    }

    /**
     * <p>
     * Compare two {@link ContainerType#getContainerTypes()} recursively.
     * <p>
     * If the common {@link ContainerType#getContainerTypes()} is empty,
     * copy all containerTypes from current
     * {@link ContainerType#getContainerTypes()} to common
     * {@link ContainerType#getContainerTypes()}.
     * 
     * <p>
     * Find out common class, of current and common
     * {@link ContainerType#getObjectClass()}es by the
     * {@link ClassService#findCommonClassType(Class, Class)} method
     * and set it to the common {@link ContainerType#setObjectClass(Class)}.
     * 
     * @param currentContainerType will be merged to commonContainerType
     * @param commonContainerType will be updated by currentContainerType
     * properties
     */
    private void mergeToCommonContainer(ContainerType currentContainerType, ContainerType commonContainerType) {
        commonContainerType.setObjectClass(ClassService.getInstance()
                .findCommonClassType(currentContainerType.getObjectClass(),
                        commonContainerType.getObjectClass()));
        if (commonContainerType.getContainerTypes().isEmpty()) {
            commonContainerType.setContainerTypes(currentContainerType.getContainerTypes());
            return;
        } else {
            for (int i = 0; i < currentContainerType.getContainerTypes().size(); i++) {
                ContainerType nextCurrentContainerType = currentContainerType.getContainerTypes().get(i);
                ContainerType nextCommonContainerType = commonContainerType.getContainerTypes().get(i);
                mergeToCommonContainer(nextCurrentContainerType, nextCommonContainerType);
            }
        }

    }

    /**
     * Find out the most general type of the {@link Map} values.
     * 
     * @param map the objects container. This method will iterate these objects
     * and find out general types of keys and values of the {@link Map}.
     * @param containerType This object will be completed by found general
     * types, se the {@link ContainerType#getContainerTypes()} method.
     * @param depth Depth of plunge this {@link ContainerType}, beginning from 1
     * If the inner items are generic, return find out their
     * {@link ContainerType}s recursively, up to defined depth.
     */
    private void findGeneralItemsTypeOfMap(Map<?, ?> map, ContainerType containerType, int depth) {
        ContainerType commonKeyContainerType = new ContainerType();
        ContainerType commonValueContainerType = new ContainerType();
        for (Entry<?, ?> entry : map.entrySet()) {
            Object keyObject = entry.getKey();
            Object valueObject = entry.getValue();

            ContainerType keyContainerType = new ContainerType();
            setContainerTypes(keyObject, keyContainerType, depth + 1);
            mergeToCommonContainer(keyContainerType, commonKeyContainerType);

            ContainerType valueContainerType = new ContainerType();
            setContainerTypes(valueObject, valueContainerType, depth + 1);
            mergeToCommonContainer(valueContainerType, commonValueContainerType);
        }
        if (commonKeyContainerType.getObjectClass() != null || commonValueContainerType.getObjectClass() != null) {
            containerType.getContainerTypes().add(commonKeyContainerType);
            containerType.getContainerTypes().add(commonValueContainerType);
        }
    }

    /**
     * @deprecated
     * Generate a generic signature of a value, for example
     * {@code <java.lang.String,java.lang.String>}.
     * @param iterable object instance as a source of the signature
     * @return for example {@code <java.lang.String>}.
     * If the inner items are generic too, return for example
     * {@code <java.util.ArrayList<java.lang.String>>}
     */
    private String generateGenericSignatureOfIterable(Iterable<?> iterable) {
        Iterator<?> iterator = iterable.iterator();
        Class<?> commonClass = null;
        boolean allValuesHasTheSameType = true;
        while (iterator.hasNext()) {
            Object object = iterator.next();
            Class<?> objectClass = object.getClass();
            commonClass = ClassService.getInstance().findCommonClassType(commonClass, objectClass);
            if (objectClass != commonClass) {
                allValuesHasTheSameType = false;
            }
        }
        if (commonClass == null) {
            throw new AppRuntimeException("closestClass cannot be null");
        }
        if (allValuesHasTheSameType) {
            String innerGenericSignature = generateGenericSignature(iterable.iterator().next());
            return IdentifierService.GENERIC_LEFT_BRACKET + commonClass.getCanonicalName() + innerGenericSignature
                    + IdentifierService.GENERIC_RIGHT_BRACKET;
        }
        return IdentifierService.GENERIC_LEFT_BRACKET + commonClass.getCanonicalName()
                + IdentifierService.GENERIC_RIGHT_BRACKET;
    }

    /**
     * @deprecated
     * Find out a class name from the identifier.
     * @param identifier generated by the {@link #generateIdentifier(Object)}
     * method.
     * @return {@link Class} name. For example {@link java.util.TreeMap}.Entry
     * will be returned from the
     * 
     * <pre>
     * {@code "java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5"}
     * </pre>
     * 
     * identifier.
     */
    public String findClassName(String identifier) {
        if (identifier.contains(GENERIC_LEFT_BRACKET)) {
            return identifier.substring(0, identifier.indexOf(GENERIC_LEFT_BRACKET));
        }
        if (identifier.contains(GENERIC_VALUE_SEPARATOR)) {
            return identifier.substring(0, identifier.indexOf(GENERIC_VALUE_SEPARATOR));
        }
        return identifier;
    }

    /**
     * @deprecated
     * Find out key class from an identifier. For example, if the identifier
     * contains
     * 
     * <pre>
     * {@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}
     * </pre>
     * 
     * String,
     * then {@link Integer#getClass()} will be returned.
     * @param identifier for example
     * 
     * <pre>
     * {@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}
     * </pre>
     * 
     * @return for example {@link Integer} class of this identifier. Return
     * 'null' if the key class can not be found.
     */
    public Class<?> findKeyClass(String identifier) {
        if (!identifier.contains(GENERIC_LEFT_BRACKET)) {
            return null;
        }
        int beginIndex = identifier.indexOf(GENERIC_LEFT_BRACKET) + GENERIC_LEFT_BRACKET.length();
        int endIndex = identifier.indexOf(GENERIC_COMMA, beginIndex);
        try {
            return Class.forName(identifier.substring(beginIndex, endIndex));
        } catch (ClassNotFoundException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * @deprecated
     * Find out value class from an identifier. For example, if the identifier
     * contains
     * 
     * <pre>
     * {@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}
     * </pre>
     * 
     * ,
     * then {@link String#getClass()} will be returned.
     * @param identifier for example
     * 
     * <pre>
     * {@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}
     * </pre>
     * 
     * @return for example {@link String} class of this identifier. Return
     * 'null' if the value class can not be found.
     */
    public Class<?> findValueClass(String identifier) {
        if (!identifier.contains(GENERIC_LEFT_BRACKET) || !identifier.contains(GENERIC_RIGHT_BRACKET)) {
            return null;
        }
        String[] parts = identifier.split(GENERIC_COMMA);
        if (parts.length != 2) {
            throw new AppRuntimeException("Only one comma expected in the identifier " + identifier);
        }

        int endIndex = parts[1].indexOf(GENERIC_RIGHT_BRACKET);
        try {
            return Class.forName(parts[1].substring(0, endIndex));
        } catch (ClassNotFoundException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * @deprecated
     * Find out {@link Type}s of generic object form the method argument. This
     * method is recursive.
     * @param identifier object identifier, for example
     * {@code "com.apache.a4javadoc.javaagent.mapper.WrapperClass@70fa8cbc"}
     * without generic types.<br>
     * Another example is a
     * {@code "java.util.Arrays$ArrayList<java.lang.String>@fe2"}
     * with a {@link String} generic type.<br>
     * More complex example is a
     * {@code "java.util.LinkedHashMap<java.lang.String,java.util.LinkedHashMap<java.lang.String,java.lang.Integer>>@27df"}
     * with a {@link String}, {@link String} and {@link Integer} generic
     * types.<br>
     * @return for example {@link Integer} and {@link String} from the
     * identifier
     * 
     * <pre>
     * {@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}
     * </pre>
     */
    public List<Class<?>> findGenericTypes(String identifier) {
        if (!identifier.contains(GENERIC_LEFT_BRACKET)) {
            return Collections.emptyList();
        }
        try {
            int beginIndex = identifier.indexOf(GENERIC_LEFT_BRACKET) + GENERIC_LEFT_BRACKET.length();
            int endIndex = identifier.lastIndexOf(GENERIC_RIGHT_BRACKET);
            String[] classes = identifier.substring(beginIndex, endIndex).split(GENERIC_COMMA);
            List<Class<?>> result = new ArrayList<>();
            for (String className : classes) {
                if (className.contains(GENERIC_LEFT_BRACKET)) {
                    result.addAll(findGenericTypes(className));
                } else {
                    result.add(Class.forName(className));
                }
            }
            return result;
        } catch (ClassNotFoundException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * @deprecated
     * Find out {@link Class} of identifier.
     * @param identifier object identifier
     * @return for example {@link java.util.TreeMap} from the identifier
     * 
     * <pre>
     * {@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}
     * </pre>
     */
    public Class<?> findClass(String identifier) {
        try {
            return Class.forName(findClassName(identifier));
        } catch (ClassNotFoundException e) {
            throw new AppRuntimeException(e);
        }
    }

    // /**
    // * @deprecated
    // * Parse an argument and create the {@link Identifier} of some instance of
    // object.
    // * @param identifierString see the {@link Identifier#getSource()} field
    // * @return {@link Identifier} - an object representation of the argument
    // */
    // public Identifier parse(String identifierString) {
    // Identifier identifier = new Identifier();
    //// identifier.setSource(identifierString);
    //
    // ContainerType containerType = parseContainerType(identifierString);
    // identifier.setContainerType(containerType);
    //
    // return identifier;
    // }

    /**
     * @deprecated
     * Parse an enclosing {@link ContainerType}.
     * @param identifierString the data source
     * @return main enclosing {@link ContainerType} from the argument
     */
    private ContainerType parseContainerType(String identifierString) {
        ContainerType containerType = new ContainerType();
        // containerType.setClassName(findClassName(identifierString));
        List<ContainerType> containerTypes = new ArrayList<>();
        containerType.setContainerTypes(containerTypes);

        int beginIndex = identifierString.indexOf(GENERIC_LEFT_BRACKET);
        if (beginIndex == -1) {
            return containerType;
        }
        int endIndex = identifierString.lastIndexOf(GENERIC_RIGHT_BRACKET);
        String inner = identifierString.substring(beginIndex + GENERIC_LEFT_BRACKET.length(), endIndex);
        parseContainerTypes(inner, containerTypes);
        return containerType;
    }

    /**
     * @deprecated
     * Recursive method.
     * @param identifierString the data source
     * @param containerTypes the result
     */
    private void parseContainerTypes(String identifierString, List<ContainerType> containerTypes) {
        List<String> parts = new ArrayList<>();
        while (true) {
            int firstCommaIndex = identifierString.indexOf(GENERIC_COMMA);
            int firstLeftBracketIndex = identifierString.indexOf(GENERIC_LEFT_BRACKET);
            if (firstLeftBracketIndex == -1) {
                parts.addAll(Arrays.asList(identifierString.split(GENERIC_COMMA)));
                break;
            }

        }
        containerTypes.add(parseContainerType(identifierString));
        // String[] parts = identifierString.split(GENERIC_COMMA);
        // for (int i = 0; i < parts.length; i++) {
        // }
    }

    /**
     * Create JSON from the {@link Identifier}. Create for example
     * 
     * <pre>
     * {"hash":"66ccfbd8","containerType":{"objectClass":"com.apache.a4javadoc.javaagent.mapper.WrapperClass","containerTypes":[]}}
     * </pre>
     * 
     * @param identifier the JSON source
     * @param jsonGenerator the JSON holder
     * 
     * @throws IOException
     */
    public void processIdentifier(Identifier identifier, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectFieldStart(CONTAINER_TYPE_FIELD_NAME);
        processIdentifierContainerType(identifier.getContainerType(), jsonGenerator);
        jsonGenerator.writeEndObject();
        jsonGenerator.writeEndObject();
    }

    /**
     * Recursive method. Create JSON from the {@link ContainerType}. Create for
     * example
     * 
     * <pre>
     * "containerType":{"objectClass":"com.apache.a4javadoc.javaagent.mapper.WrapperClass","containerTypes":[]}
     * </pre>
     * 
     * @param containerType the JSON source
     * @param jsonGenerator the JSON holder
     * @throws IOException
     */
    private void processIdentifierContainerType(ContainerType containerType, JsonGenerator jsonGenerator)
            throws IOException {
        jsonGenerator.writeStringField(OBJECT_CLASS_FIELD_NAME, containerType.getObjectClass().getName());
        jsonGenerator.writeArrayFieldStart(CONTAINER_TYPES_FIELD_NAME);
        for (ContainerType innerContainerType : containerType.getContainerTypes()) {
            jsonGenerator.writeStartObject();
            processIdentifierContainerType(innerContainerType, jsonGenerator);
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
    }

    /**
     * Create {@link Identifier} from the {@link JsonNode}
     * 
     * @param jsonNode the source
     * @return {@link Identifier} created from the {@link JsonNode}
     */
    public Identifier createIdentifierFromJsonNode(JsonNode jsonNode) {
        Identifier identifier = new Identifier();

        JsonNode containerTypeNode = jsonNode.get(CONTAINER_TYPE_FIELD_NAME);

        identifier.setContainerType(createContainerType(containerTypeNode));

        return identifier;
    }

    /**
     * Recursive method. Create {@link ContainerType} from the {@link JsonNode}.
     * 
     * @param jsonNode the source
     * @return {@link ContainerType} created from the {@link JsonNode}
     */
    private ContainerType createContainerType(JsonNode jsonNode) {
        try {
            ContainerType containerType = new ContainerType();
            containerType.setObjectClass(Class.forName(jsonNode.get(OBJECT_CLASS_FIELD_NAME).asText()));

            Iterator<JsonNode> iterator = jsonNode.get(CONTAINER_TYPES_FIELD_NAME).iterator();
            while (iterator.hasNext()) {
                containerType.getContainerTypes().add(createContainerType(iterator.next()));
            }

            return containerType;
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * Generate the {@link Identifier} from a {@link Field}. This {@link File}
     * name is defined in the {@link JsonNode}
     * and its properties defined in the parentInstance.
     * 
     * @param jsonNode contains the {@link Field} name and value.
     * @param parentInstance enclosing object of the {@link Field}. Cannot be
     * 'null'.
     * @return {@link Identifier} created from the {@link Field}
     */
    public Identifier generateIdentifier(JsonNode jsonNode, Object parentInstance) {
        if (parentInstance == null) {
            throw new AppRuntimeException("In this case the parentInstance cannot be 'null'. JsonNode: " + jsonNode);
        }
        try {
            Field field = parentInstance.getClass().getDeclaredField(jsonNode.asText());
            Identifier identifier = new Identifier();
            ContainerType containerType = new ContainerType();
            containerType.setObjectClass(field.getDeclaringClass());
            if (field.getDeclaringClass().isArray() || field.getGenericType() instanceof ParameterizedType) {
                List<Class<?>> classes = FieldService.getInstance().findArrayOrParameterizedTypeClasses(field);
                for (Class<?> clazz : classes) {
                    ContainerType nextContainerType = new ContainerType();
                    nextContainerType.setObjectClass(clazz);
                    containerType.getContainerTypes().add(nextContainerType);
                }
            }
            identifier.setContainerType(containerType);
            return identifier;
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * @deprecated
     * Generate a generic signature of a value, for example
     * {@code <java.lang.String,java.lang.String>}.
     * Process {@link Iterable}s and {@link Map}s only.
     * @param value object instance as a source of the signature
     * @return an empty string if the value is not {@link Iterable} nor
     * {@link Map}.
     */
    public String generateGenericSignature(Object value) {
        Class<?> clazz = value.getClass();

        if (Iterable.class.isAssignableFrom(clazz)) {
            Iterable<?> iterable = (Iterable<?>) value;
            return generateGenericSignatureOfIterable(iterable);
        }
        if (Map.class.isAssignableFrom(clazz)) {
            Map<?, ?> map = (Map<?, ?>) value;
            return generateGenericSignatureOfMap(map);
        }
        return EMPTY_STRING;
    }

    /**
     * @deprecated
     * Generate a generic signature of the {@link Map} from the argument.
     * @param map object instance as a source of the signature
     * @return for example {@code <java.lang.String,java.lang.String>}.<br>
     * If the inner items are generic too, return for example
     * {@code <java.lang.String,java.util.LinkedHashMap<java.lang.String,java.lang.Integer>>}
     */
    private String generateGenericSignatureOfMap(Map<?, ?> map) {
        Class<?> keyCommonClass = null;
        Class<?> valueCommonClass = null;
        boolean allKeysHasTheSameType = true;
        boolean allValuesHasTheSameType = true;
        Object theFirstKey = null;
        Object theFirstValue = null;
        for (Object object : map.entrySet()) {
            Entry<?, ?> entry = (Entry<?, ?>) object;
            // key
            Object keyObject = entry.getKey();
            if (theFirstKey == null) {
                theFirstKey = keyObject;
            }
            Class<?> keyClass = keyObject.getClass();
            keyCommonClass = ClassService.getInstance().findCommonClassType(keyCommonClass, keyClass);
            if (keyClass != keyCommonClass) {
                allKeysHasTheSameType = false;
            }
            // value
            Object valueObject = entry.getValue();
            if (theFirstValue == null) {
                theFirstValue = valueObject;
            }
            Class<?> valueClass = valueObject.getClass();
            valueCommonClass = ClassService.getInstance().findCommonClassType(valueCommonClass, valueClass);
            if (valueClass != valueCommonClass) {
                allValuesHasTheSameType = false;
            }
        }
        if (keyCommonClass == null) {
            throw new AppRuntimeException("keyCommonClass cannot be null");
        }
        if (valueCommonClass == null) {
            throw new AppRuntimeException("valueCommonClass cannot be null");
        }
        String innerKeySignature = EMPTY_STRING;
        if (allKeysHasTheSameType) {
            innerKeySignature = generateGenericSignature(theFirstKey);
        }
        String innerValueSignature = EMPTY_STRING;
        if (allValuesHasTheSameType) {
            innerValueSignature = generateGenericSignature(theFirstValue);
        }
        return IdentifierService.GENERIC_LEFT_BRACKET + keyCommonClass.getCanonicalName() + innerKeySignature
                + IdentifierService.GENERIC_COMMA + valueCommonClass.getCanonicalName() + innerValueSignature
                + IdentifierService.GENERIC_RIGHT_BRACKET;
    }

}
