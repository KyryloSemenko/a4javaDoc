package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.apache.a4javadoc.exception.AppRuntimeException;

/**
 * Stateless singleton for working objects identifier.
 * Example of identifier: <pre>java.util.TreeMap<java.lang.Integer,java.lang.String>@370b5</pre>
 * @author Kyrylo Semenko
 */
public class IdentifierService {

    /** A left bracket for example in the <pre>{@code "ArrayList<java.lang.String>}@fe2"</pre> identifier **/
    public static final String GENERIC_LEFT_BRACKET = "<";

    /** A right bracket for example in the <pre>{@code "ArrayList<java.lang.String>}@fe2"</pre> identifier **/
    public static final String GENERIC_RIGHT_BRACKET = ">";

    /** Separator of class name and hash in for example <pre>"com.apache.a4javadoc.javaagent.mapper.CircularClass@2d332eab"</pre> */
    public static final String GENERIC_VALUE_SEPARATOR = "@";

    /** A comma between generic types, for example in the <pre>{@code "java.util.TreeMap<java.lang.Integer,java.lang.String>@370b5"}</pre> identifier **/
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
     * Generate an identifier from {@link Class#getName()}
     * plus eventually generic types plus {@link #GENERIC_VALUE_SEPARATOR}
     * plus {@link Object#hashCode()} in hexadecimal form,
     * for example <pre>{@code "java.lang.String@6ed80802"}</pre>
     * or <pre>{@code "java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5"}</pre>
     * or more complex <pre>{@code "java.util.LinkedHashMap<java.lang.String,java.util.LinkedHashMap<java.lang.Integer,java.lang.String>>@27df"}</pre>
     * @param value the identifier source
     * @return the identifier of the 'value' argument
     */
    public String generateIdentifier(Object value) {
        return value.getClass().getName()
                + generateGenericSignature(value)
                + GENERIC_VALUE_SEPARATOR
                + Integer.toHexString(value.hashCode());
    }

    /**
     * Generate a generic signature of a value, for example {@code <java.lang.String,java.lang.String>}.
     * Process {@link Iterable}s and {@link Map}s only.
     * @param value object instance as a source of the signature
     * @return an empty string if the value is not {@link Iterable} nor {@link Map}.
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
     * Generate a generic signature of the {@link Map} from the argument.
     * @param map object instance as a source of the signature
     * @return for example {@code <java.lang.String,java.lang.String>}.<br>
     * If the inner items are generic too, return for example {@code <java.lang.String,java.util.LinkedHashMap<java.lang.String,java.lang.Integer>>}
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
        return IdentifierService.GENERIC_LEFT_BRACKET
                + keyCommonClass.getCanonicalName() + innerKeySignature
                + IdentifierService.GENERIC_COMMA
                + valueCommonClass.getCanonicalName() + innerValueSignature
                + IdentifierService.GENERIC_RIGHT_BRACKET;
    }

    /**
     * Generate a generic signature of a value, for example {@code <java.lang.String,java.lang.String>}.
     * @param iterable object instance as a source of the signature
     * @return for example {@code <java.lang.String>}.
     * If the inner items are generic too, return for example {@code <java.util.ArrayList<java.lang.String>>}
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
            return IdentifierService.GENERIC_LEFT_BRACKET + commonClass.getCanonicalName() + innerGenericSignature + IdentifierService.GENERIC_RIGHT_BRACKET;
        }
        return IdentifierService.GENERIC_LEFT_BRACKET + commonClass.getCanonicalName() + IdentifierService.GENERIC_RIGHT_BRACKET;
    }

    /**
     * Find out a class name from the identifier.
     * @param identifier generated by the {@link #generateIdentifier(Object)} method.
     * @return {@link Class} name. For example {@link java.util.TreeMap}.Entry will be returned from the
     * <pre>{@code "java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5"}</pre> identifier.
     */
    public String findClassName(String identifier) {
        if (identifier.contains(GENERIC_LEFT_BRACKET)) {
            return identifier.substring(0, identifier.indexOf(GENERIC_LEFT_BRACKET));
        }
        return identifier.substring(0, identifier.indexOf(IdentifierService.GENERIC_VALUE_SEPARATOR));
    }

    /**
     * Find out key class from an identifier. For example, if the identifier contains
     * <pre>{@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}</pre> String,
     * then {@link Integer#getClass()} will be returned.
     * @param identifier for example <pre>{@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}</pre>
     * @return for example {@link Integer} class of this identifier. Return 'null' if the key class can not be found.
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
     * Find out value class from an identifier. For example, if the identifier contains
     * <pre>{@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}</pre>,
     * then {@link String#getClass()} will be returned.
     * @param identifier for example <pre>{@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}</pre>
     * @return for example {@link String} class of this identifier. Return 'null' if the value class can not be found.
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
     * Find out {@link Type}s of generic object form the method argument. This method is recursive.
     * @param identifier object identifier, for example {@code "com.apache.a4javadoc.javaagent.mapper.WrapperClass@70fa8cbc"}
     * without generic types.<br>
     * Another example is a {@code "java.util.Arrays$ArrayList<java.lang.String>@fe2"}
     * with a {@link String} generic type.<br>
     * More complex example is a {@code "java.util.LinkedHashMap<java.lang.String,java.util.LinkedHashMap<java.lang.String,java.lang.Integer>>@27df"}
     * with a {@link String}, {@link String} and {@link Integer} generic types.<br>
     * @return for example {@link Integer} and {@link String} from the identifier
     * <pre>{@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}</pre>
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
     * Find out {@link Class} of identifier.
     * @param identifier object identifier 
     * @return for example {@link java.util.TreeMap} from the identifier
     * <pre>{@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}</pre>
     */
    public Class<?> findClass(String identifier) {
        try {
            return Class.forName(findClassName(identifier));
        } catch (ClassNotFoundException e) {
            throw new AppRuntimeException(e);
        }
    }



}
