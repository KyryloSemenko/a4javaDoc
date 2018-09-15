package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stateless singleton for working with java {@link Method}s.
 * @author Kyrylo Semenko
 */
public class MethodService {
    
    private static MethodService instance;
    
    private MethodService() {
        // empty
    }
    
    /**
     * @return the {@link MethodService} singleton.
     */
    public static MethodService getInstance() {
        if (instance == null) {
            instance = new MethodService();
        }
        return instance;
    }

    /**
     * <p>Find out the {@link Method} without parameters
     * with {@link Iterable} or {@link Map} return type and number of returned
     * types the same as a number of types in the 'typeVariables' argument.
     * 
     * <p>Returned method return items types should not be more general
     * then the {@link TypeVariable#getBounds()}s of items from the argument.
     * 
     * <p>If the number of items in the 'types' argument is 1,
     * returned method return type should contain items without generic types.
     * For example
     * {@code List<String>} or
     * {@code int[]}.
     * 
     * <p>If the number of items in the 'types' argument is 2,
     * returned method return type should contain the {@link Map} where keys
     * and values are not generic, or {@link List}, {@link Set}, {@link Array}
     * where items is generic and has exactly 2 inner non - generic items,
     * for example
     * {@code Map<Integer, String>} or
     * {@code List<List<String>>} or
     * {@code Object[][]..}
     * 
     * <p>A similar approach will be applied if a number of items
     * in the 'types' argument is 3 and more.
     * 
     * @param value the data source
     * @param typeVariables items described required types bounds for returning
     * by the searched method
     * @return the method which meets the requirement
     */
    public Method findIterableOrMapMethod(Object value, List<TypeVariable<?>> typeVariables) {
        for (Method method : value.getClass().getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                Object[][] o = new Object[][]{};
            }
        }
        return null;
    }
    
}
