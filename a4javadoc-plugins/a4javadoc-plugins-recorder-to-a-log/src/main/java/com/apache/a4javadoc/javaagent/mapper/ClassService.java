package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    /** Find out the first common class or interface from classes hierarchy. */
    public Class<?> findCommonParent(Class<?> classLeft, Class<?> classRight) {
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

//    // TODO Auto-generated method stub
//    private void findSuperClassesAndInterfaces(Class<?> clazz, ArrayList<Class<?>> superClassesAndInterfaces) {
//        Class<?> superClass = clazz.getSuperclass();
//        if (superClass != null) {
//            superClassesAndInterfaces.add(superClass);
//            findSuperClassesAndInterfaces(superClass, superClassesAndInterfaces);
//        }
//        Class<?>[] interfaces = clazz.getInterfaces();
//        superClassesAndInterfaces.addAll(Arrays.asList(interfaces));
//        for (Class<?> nextInterface : interfaces) {
//            findSuperClassesAndInterfaces(nextInterface, superClassesAndInterfaces);
//        }
//    }

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

    /** Call the {@link #findParent(Class, List)} method. */
    private List<Class<?>> findParents(Class<?> clazz) {
        List<Class<?>> result = new ArrayList<>();
        findParent(clazz, result);
        return result;
    }

    /** Recursive method. Add clazz super class to result. */
    private void findParent(Class<?> clazz, List<Class<?>> result) {
        Class<?> superclass = clazz.getSuperclass();
        result.add(superclass);
        if (Object.class != superclass) {
            findParent(superclass, result);
        }
    }
}
