package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

//    /** TODO */
//    private Class<?> findCommonClass(Class<?> commonClass, Class<?> clazz) {
//        if (commonClass == null) {
//            commonClass = clazz;
//        } else {
//            if (!commonClass.isAssignableFrom(clazz)) {
//                commonClass = clazz;
//            }
//        }
//        if (commonClass == null) {
//            throw new AppRuntimeException("commonClass cannot be null");
//        }
//        return commonClass;
//    }


    /**
     * Find out if the argument is {@link Array} or {@link ParameterizedType} or not
     * @param typeForReturning some type
     * @return 'true' if the argument is {@link Array} or {@link ParameterizedType}
     */
    public boolean isCollectionOrArray(Type typeForReturning) {
        return typeForReturning.getClass().isArray() || ParameterizedType.class.isAssignableFrom(typeForReturning.getClass());
    }

    public boolean isClassAssignableFromType(Class<?> clazz, Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?>) {
                Class<?> rawTypeClass = (Class<?>) rawType;
                return rawTypeClass.isAssignableFrom(clazz);
            }
        }
        if (type instanceof Class) {
            return clazz.isAssignableFrom((Class<?>)type);
        }
        throw new AppRuntimeException("Cannot determine type of " + type);
    }

}
