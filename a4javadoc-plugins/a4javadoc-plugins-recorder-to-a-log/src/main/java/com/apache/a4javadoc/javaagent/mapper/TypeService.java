package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.apache.a4javadoc.exception.AppRuntimeException;

/**
 * Stateless singleton for working with java {@link Type}s.
 * @author Kyrylo Semenko
 */
public class TypeService {
    
    private static TypeService instance;
    
    private TypeService() {
        // empty
    }
    
    /**
     * @return the {@link TypeService} singleton.
     */
    public static TypeService getInstance() {
        if (instance == null) {
            instance = new TypeService();
        }
        return instance;
    }

    /**
     * Find out if the argument is {@link Array} or {@link ParameterizedType} or not
     * @param typeForReturning some type
     * @return 'true' if the argument is {@link Array} or {@link ParameterizedType}
     */
    public boolean isCollectionOrArray(Type typeForReturning) {
        return typeForReturning.getClass().isArray() || ParameterizedType.class.isAssignableFrom(typeForReturning.getClass());
    }

    /**
     * <p>
     * Decide if the {@link Type} from the second argument
     * can be assigned to {@link Class} from the first argument.
     * 
     * @param clazz more common type
     * @param type more specific type
     * @return 'true' if {@link Class#isAssignableFrom(Class)} is 'true'.
     * If the second argument is {@link ParameterizedType}, obtain its
     * {@link ParameterizedType#getRawType()} and use it for the decision.
     */
    public boolean isClassAssignableFromType(Class<?> clazz, Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?>) {
                Class<?> rawTypeClass = (Class<?>) rawType;
                return clazz.isAssignableFrom(rawTypeClass);
            }
        }
        if (type instanceof Class) {
            return clazz.isAssignableFrom((Class<?>)type);
        }
        throw new AppRuntimeException("Cannot determine type of " + type);
    }

}
