package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.apache.a4javadoc.exception.AppRuntimeException;

/**
 * Stateless singleton for working with java {@link Type}s.
 * 
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
     * Find out if the argument is {@link Array} or {@link ParameterizedType} or
     * not
     * 
     * @param typeForReturning some type
     * @return 'true' if the argument is {@link Array} or
     * {@link ParameterizedType}
     */
    public boolean isCollectionOrArray(Type typeForReturning) {
        return typeForReturning.getClass().isArray()
                || ParameterizedType.class.isAssignableFrom(typeForReturning.getClass());
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
            return clazz.isAssignableFrom((Class<?>) type);
        }
        throw new AppRuntimeException("Cannot determine type of " + type);
    }

    /**
     * <p>
     * Find out {@link ParameterizedType#getActualTypeArguments()} of
     * {@link Class#getGenericSuperclass()} of {@link Class} from the argument.
     * 
     * @param clazz the data source
     * @return found {@link TypeVariable}s or an empty {@link List}
     */
    public List<TypeVariable<?>> collectTypeVariables(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass != null && ParameterizedType.class.isAssignableFrom(genericSuperclass.getClass())) {
            Type[] types = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
            List<TypeVariable<?>> typeVariables = new ArrayList<>();
            for (Type type : types) {
                if (type instanceof TypeVariable<?>) {
                    @SuppressWarnings("unchecked")
                    TypeVariable<Class<?>> typeVariable = (TypeVariable<Class<?>>) type;
                    typeVariables.add(typeVariable);
                } else {
                    throw new AppRuntimeException("The Type should be TypeVariable. Type: " + type);
                }
            }
            return typeVariables;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * <p>
     * Find out generic {@link Class} types from an object returned by the
     * {@link Method} invocation.
     * <p>
     * Invoke the {@link Method} from the first argument, obtain its value,
     * create a new empty {@link ContainerType}, call the
     * {@link IdentifierService#setContainerTypes(Object, ContainerType, int)}
     * method and then collect leafs by calling the
     * {@link #collectLeafTypes(ContainerType, List)} method.
     * <p>
     * For example if the {@link Method} is getMap() and returned map contains
     * {@link Integer} keys and {@link String} values, then the returned list
     * will contain two items - {@link Integer} and {@link String}.
     * 
     * @param method the {@link Method} for invocation
     * @param value the object instance on which the method to be invoked
     * @return the most general types of objects the {@link Method} returns
     */
//    public void collectReturnTypes(Method method, Object value, ContainerType containerType) {
//        try {
//            IdentifierService.getInstance().setContainerTypes(method.invoke(value), containerType, 0);
//            List<Class<?>> classes = new ArrayList<>();
//            collectLeafTypes(containerType, classes);
//            return classes;
//        } catch (Exception e) {
//            throw new AppRuntimeException(e);
//        }
//    }

}
