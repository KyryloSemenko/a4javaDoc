package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Stateless singleton for working with {@link Constructor}s.
 * @author Kyrylo Semenko
 */
public class ConstructorService {
    
    private static ConstructorService instance;
    
    private ConstructorService() {
        // empty
    }
    
    /**
     * @return the {@link ConstructorService} singleton.
     */
    public static ConstructorService getInstance() {
        if (instance == null) {
            instance = new ConstructorService();
        }
        return instance;
    }

    /**
     * Find out {@link Constructor} for invocation.
     * @param field if clazz parameter does not contains suited constructor, the field is the source of constructors
     * @param clazz if not null, it is the source of constructors
     * @param classForReturning what type the constructor should return
     * @param parameters parameters for invocation of the constructor
     * @return the {@link Constructor} that is suitable as defined in {@link #isConstructorSuit(Class, List, Constructor)}.
     */
    public Constructor<?> findConstructor(Field field, Class<?> clazz, Class<?> classForReturning, List<Object> parameters) {
        if (clazz != null) {
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (isConstructorSuit(classForReturning, parameters, constructor)) {
                    return constructor;
                }
            }
        }
        if (field != null) {
            for (Constructor<?> constructor : field.getType().getDeclaredConstructors()) {
                if (isConstructorSuit(classForReturning, parameters, constructor)) {
                    return constructor;
                }
            }
        }
        if (clazz != null) {
            return findConstructor(field, clazz.getEnclosingClass(), classForReturning, parameters);
        }
        return null;
    }

    /**
     * Find out if the constructor from argument is invokable with this parameters
     * and it will return required typeForReturning
     * @param classForReturning required type for returning from the constructor
     * @param parameters the constructor parameters
     * @param constructor adept for invocation
     * @return 'true' if this constructor has the same parameter types as this method 'parameters' argument
     * and its return type is the same as classForReturning
     */
    private boolean isConstructorSuit(Class<?> classForReturning, List<Object> parameters, Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        return ParameterService.getInstance().isParametersSuit(parameterTypes, parameters, constructor.isVarArgs()) ||
                (constructor.isVarArgs()
                && constructor.getDeclaringClass() == classForReturning
                && parameterTypes[0] == parameters.get(0).getClass());
    }

}
