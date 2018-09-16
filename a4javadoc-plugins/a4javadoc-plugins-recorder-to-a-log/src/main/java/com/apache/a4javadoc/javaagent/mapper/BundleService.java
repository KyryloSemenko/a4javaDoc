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

    /**
     * <p>
     * If the instance from the first argument is {@link Array}, call the
     * {@link #addArrayItemsToList(Object, List)} method.
     * 
     * <p>
     * If the instance from the first argument is {@link Iterable}, call the
     * {@link #addIterableItemsToList(Iterable, List)} method.
     * <p>
     * 
     * If the instance from the first argument is {@link Map}, call the
     * {@link #addMapItemsToList(Map, List)} method.
     * 
     * @param instance the data source
     * @param objectList the list for completion of items
     */
    public void addItemsToList(Object instance, List<Object> objectList) {
        if (instance.getClass().isArray()) {
            addArrayItemsToList(instance, objectList);
        } else if (Iterable.class.isAssignableFrom(instance.getClass())) {
            addIterableItemsToList((Iterable<?>) instance, objectList);
        } else if (Map.class.isAssignableFrom(instance.getClass())) {
            addMapItemsToList((Map<?,?>) instance, objectList);
        }
    }

    /**
     * Call the {@link Map#entrySet()} method
     * and add items to objectList.
     * 
     * @param map the source of items
     * @param objectList the list for completion of items
     */
    public void addMapItemsToList(Map<?,?> map, List<Object> objectList) {
        for (Object entry : map.entrySet()) {
            objectList.add(entry);
        }
    }

    /**
     * Call the {@link Iterable#iterator()} method
     * and add items to objectList.
     * 
     * @param iterable the source of items
     * @param objectList the list for completion of items
     */
    public void addIterableItemsToList(Iterable<?> iterable, List<Object> objectList) {
        Iterator<?> iterator = iterable.iterator();
        while(iterator.hasNext()) {
            Object nextObject = iterator.next();
            objectList.add(nextObject);
        }
    }

    /**
     * Iterate items of the {@link Array} from the first argument
     * and add items to the list from the second argument.
     * 
     * @param arrayObject the source of items
     * @param objectList the list for completion of items
     */
    public void addArrayItemsToList(Object arrayObject, List<Object> objectList) {
        int length = Array.getLength(arrayObject);
        for (int i = 0; i < length; i++) {
            Object arrayElement = Array.get(arrayObject, i);
            objectList.add(arrayElement);
        }
    }

}
