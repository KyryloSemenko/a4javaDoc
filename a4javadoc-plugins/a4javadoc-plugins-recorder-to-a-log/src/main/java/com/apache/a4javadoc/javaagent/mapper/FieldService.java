package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
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
     * Get fields from a source instance.
     * @param sourceObject the fields source
     * @return fields from this sourceObjecta and all its parents recursively
     * 
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
     * @return false if the field is synthetic or static
     */
    public boolean isValidField(Field field) {
        return !field.isSynthetic() && !Modifier.isStatic(field.getModifiers());
    }

    /** TODO Kyrylo Semenko stejny jako {@link #getContainerType(Field, Object)}? */
    public Class<? extends Object> getContainerType(Field field) {
        // Collections
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType parameterizedFieldType = (ParameterizedType) field.getGenericType();
            return (Class<?>) parameterizedFieldType.getActualTypeArguments()[0];
        }
        // Arrays or other objects
        return field.getType();
    }

    /** TODO Kyrylo Semenko  */
    public Type[] getContainerType(Field field, Object fieldObject) {
        if (fieldObject != null) {
            // Arrays
            if (fieldObject.getClass().isArray()) {
                return new Type[]{(fieldObject.getClass().getComponentType())};
            }
            
            // Collections and maps
            List<Object> objectList = new ArrayList<>();
            BundleService.getInstance().addItemsToList(fieldObject, objectList);
            
            // Iterate objects and find out it types, then choose the most generic
            Class<?> result = null;
            for (Object object : objectList) {
                if (result == null) {
                    result = object.getClass();
                } else {
                    if (object.getClass().isAssignableFrom(result)) {
                        result = object.getClass();
                    } else if (!result.isAssignableFrom(object.getClass())) {
                        throw new AppRuntimeException("One collection or map should not have items with different types");
                    }
                }
            }
            return new Type[]{result};
        }
        if (field != null) {
            // Arrays
            if (field.getType().isArray()) {
                return new Type[]{(field.getType().getComponentType())};
            }
            // Collections and Maps
            Type type = field.getGenericType();
            if (type instanceof ParameterizedType) {
                return ((ParameterizedType) type).getActualTypeArguments();
            }
        }
        // Other objects
        throw new AppRuntimeException("The field '" + field + "' is not an Array nor ParameterizedType");
    }
}
