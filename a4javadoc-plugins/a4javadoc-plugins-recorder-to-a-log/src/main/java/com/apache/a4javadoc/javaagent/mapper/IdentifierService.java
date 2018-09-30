package com.apache.a4javadoc.javaagent.mapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

    /**
     * See the {@link ContainerType#setFilling(String)} field description.
     */
    private static final String FILLING_FIELD_NAME = "filling";

    /**
     * See the {@link ContainerType#setFactory(String)} field description.
     */
    private static final String FACTORY_FIELD_NAME = "factory";

    /**
     * See the {@link ContainerType#setContainerTypes(List)} field description.
     */
    private static final String CONTAINER_TYPES_FIELD_NAME = "containerTypes";

    /**
     * See the {@link ContainerType#setObjectClass(Class)} field description.
     */
    private static final String OBJECT_CLASS_FIELD_NAME = "objectClass";

    /**
     * See the {@link Identifier#setContainerType(ContainerType)} field description.
     */
    private static final String CONTAINER_TYPE_FIELD_NAME = "containerType";

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
     * Find out generic types of the value, for example if the value is a map
     * with {@link String} keys and {@link Integer} values, this method will
     * fill out the {@link ContainerType} by the two inner
     * {@link ContainerType}s:
     * <pre>
     * "containerType": {
     *     {
     *         "objectClass": "java.util.LinkedHashMap",
     *         "containerTypes": [{
     *             "objectClass": "java.lang.String",
     *             "containerTypes": []
     *         },
     *         {
     *             "objectClass": "java.lang.Integer",
     *             "containerTypes": []
     *         }]
     *     }
     * }
     * </pre>
     * 
     * @param value object instance as a source of the generic types. If the
     * value is 'null', it will be skipped.
     * @param containerType this object will be completed by generic types, see
     * {@link ContainerType#setContainerTypes(List)}.
     * @param depth a plunging depth of this {@link ContainerType}, beginning
     * from 1. Max depth is defined in {@link ConfigService#getMaxDepth()}.
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
                List<TypeVariable<?>> typeVariables = TypeService.getInstance().collectTypeVariables(clazz);
                MethodService.getInstance().fillGeneralItemsTypeOfParameterized(value, typeVariables, containerType);
                return;
            }
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
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
                .findCommonClassType(currentContainerType.getObjectClass(), commonContainerType.getObjectClass()));
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
     * Create JSON from the {@link Identifier}. Create for example
     * 
     * <pre>
     * {"containerType":{"objectClass":"com.apache.a4javadoc.javaagent.mapper.WrapperClass","containerTypes":[]}}
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
        if (containerType.getFactory() != null) {
            jsonGenerator.writeStringField(FACTORY_FIELD_NAME, containerType.getFactory());
        }
        if (containerType.getFilling() != null) {
            jsonGenerator.writeStringField(FILLING_FIELD_NAME, containerType.getFilling());
        }
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
     * Recursive method.
     * <p>
     * Collect only those classes from containerType in argument that has no
     * leafs. It means collect only {@link ContainerType#getObjectClass()} of
     * {@link ContainerType} that has no inner
     * {@link ContainerType#getContainerTypes()}.
     * 
     * @param containerType the data source
     * @param classes the list for filling with return values
     */
    public void collectLeafTypes(ContainerType containerType, List<Class<?>> classes) {
        if (containerType.getContainerTypes().isEmpty()) {
            classes.add(containerType.getObjectClass());
        } else {
            for (ContainerType nextContainerType : containerType.getContainerTypes()) {
                collectLeafTypes(nextContainerType, classes);
            }
        }
    }

}
