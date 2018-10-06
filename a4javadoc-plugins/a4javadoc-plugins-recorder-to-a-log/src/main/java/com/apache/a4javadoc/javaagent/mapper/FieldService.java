package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.apache.a4javadoc.exception.AppRuntimeException;

/**
 * Stateless singleton for working with java {@link Field}s.
 * @author Kyrylo Semenko
 */
public class FieldService {

    private static FieldService instance;

    private FieldService() {
        // empty
    }

    /**
     * @return the {@link FieldService} singleton.
     */
    public static FieldService getInstance() {
        if (instance == null) {
            instance = new FieldService();
        }
        return instance;
    }

    /**
     * Get fields of an object instance from the argument.
     * @param sourceObject the fields source
     * @return fields from this sourceObjecta and all its parents recursively
     */
    public List<Field> getFields(Object sourceObject) {
        List<Field> result = new ArrayList<>();
        for (Class<?> c = sourceObject.getClass(); c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field classField : fields) {
                if (isValidField(classField)) {
                    result.add(classField);
                }
            }
        }
        return result;
    }

    /**
     * Has the field to be serialized?
     * @param field a data source
     * @return 'false' if the field is synthetic or static or transient
     */
    private boolean isValidField(Field field) {
        return !field.isSynthetic()
            && !Modifier.isStatic(field.getModifiers())
            && !Modifier.isTransient(field.getModifiers());
    }

    /**
     * Collect types of the identifier or fieldObject or field from arguments.
     * If the fieldObject argument is not null, process it.
     * Else process field argument.
     * @param field the data source
     * @param fieldObject the data source
     * @return {@link Class}es of parameters for {@link ParameterizedType}s
     * or a single {@link Class} for other data sources
     */
    public List<Class<?>> getContainerTypes(Field field, Object fieldObject) {
        if (field == null && fieldObject == null) {
            throw new AppRuntimeException("The both field and fieldObject are null");
        }
        List<Class<?>> result = new ArrayList<>();
        if (fieldObject != null) {
            // Arrays
            if (fieldObject.getClass().isArray()) {
                result.add(fieldObject.getClass().getComponentType());
                return result;
            }

            // Collections and maps
            List<Object> objectList = new ArrayList<>();
            BundleService.getInstance().addItemsToList(fieldObject, objectList);

            // Iterate objects and find out their types, then choose the most generic
            Class<?> clazz = null;
            for (Object object : objectList) {
                clazz = ClassService.getInstance().findCommonClassType(object.getClass(), clazz);
            }
            result.add(clazz);
            return result;
        }
        return findArrayOrParameterizedTypeClasses(field);
    }

    /**
     * If the {@link Field} is an array, return {@link Class#getComponentType()}.<br>
     * If the {@link Field} is a {@link ParameterizedType}, return {@link ParameterizedType#getActualTypeArguments()}.
     * @param field the source
     * @return the {@link List} of classes or the empty {@link List}
     */
    public List<Class<?>> findArrayOrParameterizedTypeClasses(Field field) {
        List<Class<?>> result = new ArrayList<>();
        // Arrays
        if (field.getType().isArray()) {
            result.add(field.getType().getComponentType());
        }
        // Collections and Maps
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type nextType : types) {
                result.add((Class<?>) nextType);
            }
        }
        return result;
    }

}
