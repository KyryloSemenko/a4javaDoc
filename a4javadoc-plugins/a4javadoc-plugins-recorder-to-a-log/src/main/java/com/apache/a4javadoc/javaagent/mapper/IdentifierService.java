package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Stateless singleton for working objects identifier.
 * Example of identifier: <pre>java.util.TreeMap<java.lang.Integer,java.lang.String>@370b5</pre>
 * @author Kyrylo Semenko
 */
public class IdentifierService {

    public static final String GENERIC_LEFT_BRACKET = "<";

    public static final String GENERIC_RIGHT_BRACKET = ">";

    /** Separator of class name and hash in for example <pre>com.apache.a4javadoc.javaagent.mapper.CircularClass@2d332eab</pre> */
    public static final String GENERIC_VALUE_SEPARATOR = "@";

    public static final String GENERIC_COMMA = ",";
    
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
     * TODO without generic types
     * Generate an identifier from {@link Class#getName()}
     * plus eventually generic types plus {@link #GENERIC_VALUE_SEPARATOR}
     * plus {@link Object#hashCode()} in hexadecimal form,
     * for example <pre>{@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}</pre>
     */
    public String generateIdentifier(Object value) {
        // TODO zvazit pridani generic, napriklad misto
        // java.util.TreeMap@370b5
        // bude
        // java.util.TreeMap<int,java.lang.String>@370b5
        return value.getClass().getName()
                + TypeService.getInstance().findOutGenericTypesSignature(value)
                + GENERIC_VALUE_SEPARATOR
                + Integer.toHexString(value.hashCode());
    }

    /** TODO */
    public String findClassName(String genericKeyValue) {
        if (genericKeyValue.contains(GENERIC_LEFT_BRACKET)) {
            return genericKeyValue.substring(0, genericKeyValue.indexOf(GENERIC_LEFT_BRACKET));
        }
        return genericKeyValue.substring(0, genericKeyValue.indexOf(IdentifierService.GENERIC_VALUE_SEPARATOR));
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
     * Find out {@link Type}s of generic object form identifier.
     * @param identifier object identifier 
     * @return for example {@link Integer} and {@link String} from the identifier
     * <pre>{@code java.util.TreeMap$Entry<java.lang.Integer,java.lang.String>@370b5}</pre>
     */
    public List<Class<?>> findGenericTypes(String identifier) {
        if (!identifier.contains(GENERIC_LEFT_BRACKET)) {
            return Collections.emptyList();
        }
        try {
            int beginIndex = identifier.indexOf(GENERIC_LEFT_BRACKET) + GENERIC_LEFT_BRACKET.length();
            int endIndex = identifier.indexOf(GENERIC_RIGHT_BRACKET);
            String[] classes = identifier.substring(beginIndex, endIndex).split(GENERIC_COMMA);
            List<Class<?>> result = new ArrayList<>();
            for (String className : classes) {
                result.add(Class.forName(className));
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
