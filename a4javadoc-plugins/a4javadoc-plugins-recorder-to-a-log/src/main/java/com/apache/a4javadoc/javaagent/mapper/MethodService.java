package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.apache.a4javadoc.exception.AppRuntimeException;

/**
 * Stateless singleton for working with java {@link Method}s.
 * 
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
     * <p>
     * Find out a {@link Method} without parameters,
     * with {@link Iterable} or {@link Map} return type and number of returned
     * types the same as a number of types in the 'typeVariables' argument.
     * 
     * @param value the data source
     * @param typeVariables items described required bounds for returning
     * by a searched method
     * @param containerType contains information about generic types included to
     * the value
     */
    public void fillGeneralItemsTypeOfParameterized(Object value, List<TypeVariable<?>> typeVariables,
            ContainerType containerType) {
        try {
            for (Method method : value.getClass().getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length == 0
                        && void.class != method.getReturnType()
                        && (Iterable.class.isAssignableFrom(method.getReturnType())
                                || Map.class.isAssignableFrom(method.getReturnType()))
                        && canTheMethodToBeDisassembler(value, typeVariables, containerType, method)) {

                    return;
                }
            }
            throw new AppRuntimeException("Cannot set up Identifier of generic type. Value: " + value);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * <p>
     * Find out if the {@link Method} from arguments can be used for obtaining
     * an object with {@link Iterable} or {@link Map} return type and number
     * of returned types the same as a number of types in the 'typeVariables'
     * argument.
     * 
     * <p>
     * Returned method return items types should not be more general
     * then the {@link TypeVariable#getBounds()}s of items from the argument.
     * 
     * <p>
     * If the number of items in the 'types' argument is 1,
     * returned method return type should contain items without generic types.
     * For example
     * {@code List<String>} or
     * {@code int[]}.
     * 
     * <p>
     * If the number of items in the 'types' argument is 2,
     * returned method return type should contain the {@link Map} where keys
     * and values are not generic, or {@link List}, {@link Set}, {@link Array}
     * where items is generic and has exactly 2 inner non - generic items,
     * for example
     * {@code Map<Integer, String>} or
     * {@code List<List<String>>} or
     * {@code Object[][]..}
     * 
     * <p>
     * A similar approach will be applied if a number of items
     * in the 'types' argument is 3 and more.
     * 
     * @param value the data source
     * @param typeVariables items described required bounds for returning
     * by a searched method
     * @param containerType contains information about generic types included to
     * the value
     * @param method the {@link Method} of the value object to be checked, if it
     * can be a disassemble method for its items.
     * @return 'true' if the {@link Method} can be used to iterate items of the
     * value
     */
    public boolean canTheMethodToBeDisassembler(Object value, List<TypeVariable<?>> typeVariables,
            ContainerType containerType, Method method) {

        try {
            ContainerType tempContainerType = new ContainerType();
            IdentifierService.getInstance().setContainerTypes(method.invoke(value), tempContainerType, 0);
            List<Class<?>> returnClasses = new ArrayList<>();
            IdentifierService.getInstance().collectLeafTypes(tempContainerType, returnClasses);
            boolean boundsMatches = true;
            if (returnClasses.size() == typeVariables.size()) {
                for (int i = 0; (boundsMatches && i < typeVariables.size()); i++) {
                    TypeVariable<?> left = typeVariables.get(i);
                    Class<?> right = returnClasses.get(i);
                    if (left.getBounds().length != 1) {
                        boundsMatches = false;
                    } else {
                        if (!((Class<?>) left.getBounds()[0]).isAssignableFrom(right)) {
                            boundsMatches = false;
                        }
                    }
                }
            } else {
                boundsMatches = false;
            }
            if (boundsMatches) {
                containerType.setContainerTypes(tempContainerType.getContainerTypes());
                containerType.setDisassembleMethod(method);
                Method factoryMethod = findFactoryMethod(value);
                containerType.setFactory(factoryMethod.getName());
                Method fillingMethod = findFillingMethod(value, containerType);
                containerType.setFilling(fillingMethod.getName());
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * Find out a method, that will be used in a deserialization for putting
     * elements to a deserialized object. For example the
     * {@code public java.lang.Object
     * com.google.common.collect.HashBasedTable.put(java.lang.Object,
     * java.lang.Object,java.lang.Object)} method is used for putting values to
     * a Guava table.
     * 
     * @param value the expected original object for validation purposes
     * @param containerType contains argument types of filling method
     */
    private Method findFillingMethod(Object value, ContainerType containerType) {
        try {
            Method factoryMethod = value.getClass().getMethod(containerType.getFactory());
            Object factoredInstance = factoryMethod.invoke(value);
            for (Method method : value.getClass().getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    List<Class<?>> parameterTypes = new ArrayList<>();
                    IdentifierService.getInstance().collectLeafTypes(containerType, parameterTypes);
                    if (parametersMatch(parameterTypes.toArray(new Class[0]), method.getParameterTypes())) {
                        Object sampleReturnedObjects = containerType.getDisassembleMethod().invoke(value);
                        Object sampleReturnedItem = BundleService.getInstance().getFirstItem(sampleReturnedObjects);
                        Object[] sampleArgsReturned = BundleService.getInstance().flattenOut(sampleReturnedItem,
                                parameterTypes);
                        method.invoke(factoredInstance, sampleArgsReturned);
                        Object invokedReturnedObjects = containerType.getDisassembleMethod().invoke(factoredInstance);
                        Object invokedReturnedItem = BundleService.getInstance().getFirstItem(invokedReturnedObjects);
                        Object[] invokedArgsReturned = BundleService.getInstance().flattenOut(invokedReturnedItem,
                                parameterTypes);
                        checkEquality(sampleArgsReturned, invokedArgsReturned);
                        return method;
                    }
                }
            }
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
        throw new AppRuntimeException("Cannot find out the filling method");
    }

    /**
     * Check if the objects inside the arguments are equals
     * 
     * @param leftArgs left argument
     * @param rightArgs right argument
     * @throws AppRuntimeException if some objects inside arguments are not
     * equals
     */
    public void checkEquality(Object[] leftArgs, Object[] rightArgs) {
        for (int i = 0; i < rightArgs.length; i++) {
            Object original = rightArgs[i];
            Object created = leftArgs[i];
            if (!original.equals(created)) {
                throw new AppRuntimeException("Original and created objects are different. Original: "
                        + original + ", created: " + created);
            }
        }
    }

    /**
     * Check out if the methodParameterTypes match to requiredParameterTypes.
     * They should have the same length and types in bounds of required types.
     * 
     * @param methodParameterTypes checked parameter types
     * @param requiredParameterTypes sample parameter types
     * @return 'true' if parameters number is the same and method parameter
     * types are in bounds of requiredParameterTypes
     */
    private boolean parametersMatch(Class<?>[] methodParameterTypes, Class<?>[] requiredParameterTypes) {
        if (methodParameterTypes.length != requiredParameterTypes.length) {
            return false;
        }
        for (int i = 0; i < requiredParameterTypes.length; i++) {
            if (!requiredParameterTypes[i].isAssignableFrom(methodParameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find out a static method for deserialization, which returns an object of
     * the value type.
     * 
     * @param value serialized object
     */
    private Method findFactoryMethod(Object value) {
        try {
            for (Method method : value.getClass().getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 0 && method.getReturnType().isAssignableFrom(value.getClass())) {
                        return method;
                    }
                }
            }
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
        throw new AppRuntimeException("Cannot find out the factory method");
    }

}
