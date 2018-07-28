package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Stateless singleton for working with java arrays, {@link Iterable}s and {@link Map}s.
 * @author Kyrylo Semenko
 */
public class BundleService {
    
    private static BundleService instance;
    
    private BundleService() {
        // empty
    }
    
    /**
     * @return the {@link BundleService} singleton.
     */
    public static BundleService getInstance() {
        if (instance == null) {
            instance = new BundleService();
        }
        return instance;
    }

    /** TODO */
    public void addItemsToList(Object instance, List<Object> objectList) {
        if (instance.getClass().isArray()) {
            addArrayItemsToList(instance, objectList);
        } else if (Iterable.class.isAssignableFrom(instance.getClass())) {
            addIterableItemsToList(instance, objectList);
        } else if (Map.class.isAssignableFrom(instance.getClass())) {
            addMapItemsToList(instance, objectList);
        }
    }

    // TODO Auto-generated method stub
    public void addMapItemsToList(Object instance, List<Object> objectList) {
        for (Object entry : ((Map<?,?>) instance).entrySet()) {
            objectList.add(entry);
        }
    }

    /** TODO */
    public void addIterableItemsToList(Object instance, List<Object> objectList) {
        Iterator<?> iterator = ((Iterable<?>) instance).iterator();
        while(iterator.hasNext()) {
            Object nextObject = iterator.next();
            objectList.add(nextObject);
        }
    }

    /** TODO */
    public void addArrayItemsToList(Object fieldObject, List<Object> objectList) {
        int length = Array.getLength(fieldObject);
        for (int i = 0; i < length; i++) {
            Object arrayElement = Array.get(fieldObject, i);
            objectList.add(arrayElement);
        }
    }

}
