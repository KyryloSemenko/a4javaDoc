package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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
    public boolean isValidField(Field field) {
        return !field.isSynthetic()
                && !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers());
    }

    /**
     * Collect types of the identifier or fieldObject or field from arguments.<br>
     * If the identifier argument is not null, process it.<br>
     * Else if the fieldObject argument is not null, process it.<br>
     * Else process field argument.<br>
     * @param field the data source
     * @param fieldObject the data source
     * @param identifier  the data source
     * @return {@link Class}es of parameters for {@link ParameterizedType}s or a single {@link Class} for other data sources
     */
    public List<Class<?>> getContainerTypes(Field field, Object fieldObject, String identifier) {
        if (identifier != null) {
            return IdentifierService.getInstance().findGenericTypes(identifier);
        }
        if (fieldObject != null) {
            // Arrays
            if (fieldObject.getClass().isArray()) {
                return Arrays.asList(fieldObject.getClass().getComponentType());
            }
            
            // Collections and maps
            List<Object> objectList = new ArrayList<>();
            BundleService.getInstance().addItemsToList(fieldObject, objectList);
            
            // Iterate objects and find out it types, then choose the most generic
            Class<?> result = null;
            for (Object object : objectList) {
                result = ClassService.getInstance().findCommonClassType(object.getClass(), result);
            }
            return Arrays.asList(result);
        }
        if (field != null) {
            // Arrays
            if (field.getType().isArray()) {
                return Arrays.asList(field.getType().getComponentType());
            }
            // Collections and Maps
            Type type = field.getGenericType();
            if (type instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                List<Class<?>> result = new ArrayList<>();
                for (Type nextType : types) {
                    result.add((Class<?>) nextType);
                }
                return result;
            }
        }
        // Other objects
        throw new AppRuntimeException("The field '" + field + "' is not Array nor ParameterizedType");
    }

}
