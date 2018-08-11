package com.apache.a4javadoc.javaagent.mapper;

import java.util.ArrayList;
import java.util.List;

import com.apache.a4javadoc.exception.AppRuntimeException;

/**
 * Stateless singleton for working with java {@link Class}es.
 * @author Kyrylo Semenko
 */
public class ClassService {
    
    private static ClassService instance;
    
    private ClassService() {
        // empty
    }
    
    /**
     * @return the {@link ClassService} singleton.
     */
    public static ClassService getInstance() {
        if (instance == null) {
            instance = new ClassService();
        }
        return instance;
    }

    /**
     * Find out the first common class or interface from classes hierarchy.
     * @param classLeft the first class the common class to look for
     * @param classRight the second class the common class to look for
     * @return common class in sense of the {@link Class#isAssignableFrom(Class)} method
     */
    public Class<?> findCommonClassType(Class<?> classLeft, Class<?> classRight) {
        if (classLeft == null && classRight == null) {
            return null;
        }
        if (classLeft == null) {
            return classRight;
        }
        if (classRight == null) {
            return classLeft;
        }
        if (classLeft.isAssignableFrom(classRight)) {
            return classLeft;
        } else if (classRight.isAssignableFrom(classLeft)) {
            return classRight;
        } else {
            List<Class<?>> leftClasses = findParents(classLeft);
            List<Class<?>> rightClasses = findParents(classRight);
            return findCommonClass(leftClasses, rightClasses);
        }
    }

    /**
     * Find out first common class
     * @param leftList the first list
     * @param rightList the second list
     * @return the first common class from two lists
     */
    private Class<?> findCommonClass(List<Class<?>> leftList, List<Class<?>> rightList) {
        for (Class<?> leftClass : leftList) {
            for (Class<?> rightClass : rightList) {
                if (leftClass == rightClass) {
                    return leftClass;
                }
            }
        }
        throw new AppRuntimeException("Cannot find common class from two lists: " + leftList.toString() + " and " + rightList.toString());
    }

    /**
     * Call the {@link #findParent(Class, List)} method.
     * @param clazz the type to look for
     * @return all parents, see a {@link #findParent(Class, List)} method.
     */
    public List<Class<?>> findParents(Class<?> clazz) {
        List<Class<?>> result = new ArrayList<>();
        findParent(clazz, result);
        return result;
    }

    /**
     * Recursive method. Add clazz super class to result.
     * @param clazz the type to look for
     * @param result found classes
     */
    private void findParent(Class<?> clazz, List<Class<?>> result) {
        if (clazz == null) {
            return;
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            result.add(superclass);
            if (Object.class != superclass) {
                findParent(superclass, result);
            }
        }
    }

    /**
     * Find out the most generic Class of the instances
     * @param instances list of objects with the sane or different types
     * @return {@link #findCommonClassType(Class, Class)}
     */
    public Class<?> findCommonClass(List<Object> instances) {
        Class<?> result = null;
        for (Object object : instances) {
            result = findCommonClassType(result, object.getClass());
        }
        return result;
    }

    /**
     * Compares two classes after application a {@link #toWrapper(Class)} method.
     * @param left the first class
     * @param right the second class
     * @return 'true' if the first and the second wrapped classes the same. For example <b>int</b> is the same as {@link Integer}
     */
    public boolean isClassesTheSame(Class<?> left, Class<?> right) {
        return toWrapper(left) == toWrapper(right);
    }
    
    /**
     * If the type is primitive, wrap it to the wrapper.
     * @param type primitive or wrapper
     * @return wrapper. For example return {@link Integer} for <b>int</b> type.
     */
    private Class<?> toWrapper(Class<?> type) {
        if (boolean.class == type) {
            return Boolean.class;
        }
        
        if (byte.class == type) {
            return Byte.class;
        }
        
        if (short.class == type) {
            return Short.class;
        }
        
        if (int.class == type) {
            return Integer.class;
        }
        
        if (long.class == type) {
            return Long.class;
        }
        
        if (float.class == type) {
            return Float.class;
        }
        
        if (double.class == type) {
            return Double.class;
        }
        
        return type;
    }
}
