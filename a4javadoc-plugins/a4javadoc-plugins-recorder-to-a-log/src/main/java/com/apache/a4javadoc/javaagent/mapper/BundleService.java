package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.apache.a4javadoc.exception.AppRuntimeException;

/**
 * Stateless singleton for working with java arrays, {@link Iterable}s and
 * {@link Map}s.
 * 
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
     * @param bundle the data source
     * @param objectList the list for completion of items
     */
    public void addItemsToList(Object bundle, List<Object> objectList) {
        if (bundle.getClass().isArray()) {
            addArrayItemsToList(bundle, objectList);
        } else if (Iterable.class.isAssignableFrom(bundle.getClass())) {
            addIterableItemsToList((Iterable<?>) bundle, objectList);
        } else if (Map.class.isAssignableFrom(bundle.getClass())) {
            addMapItemsToList((Map<?, ?>) bundle, objectList);
        }
    }

    /**
     * Call the {@link Map#entrySet()} method
     * and add items to objectList.
     * 
     * @param map the source of items
     * @param objectList the list for completion of items
     */
    private void addMapItemsToList(Map<?, ?> map, List<Object> objectList) {
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
    private void addIterableItemsToList(Iterable<?> iterable, List<Object> objectList) {
        Iterator<?> iterator = iterable.iterator();
        while (iterator.hasNext()) {
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
    private void addArrayItemsToList(Object arrayObject, List<Object> objectList) {
        int length = Array.getLength(arrayObject);
        for (int i = 0; i < length; i++) {
            Object arrayElement = Array.get(arrayObject, i);
            objectList.add(arrayElement);
        }
    }

    /**
     * Return the first item from the argument.
     * 
     * @param bundle an {@link Array} or {@link List} or {@link Map}
     * @return the first item from the argument
     */
    public Object getFirstItem(Object bundle) {
        if (bundle.getClass().isArray()) {
            if (Array.getLength(bundle) == 0) {
                return null;
            }
            return Array.get(bundle, 0);
        } else if (Iterable.class.isAssignableFrom(bundle.getClass())) {
            Iterable<?> iterable = (Iterable<?>) bundle;
            Iterator<?> iterator = iterable.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                return null;
            }
        } else if (Map.class.isAssignableFrom(bundle.getClass())) {
            Map<?, ?> map = (Map<?, ?>) bundle;
            if (map.isEmpty()) {
                return null;
            } else {
                return map.entrySet().iterator().next();
            }
        }
        throw new AppRuntimeException("Expected an Array or Iterable or Map, but found " + bundle.getClass());
    }

    /**
     * Flatten out objects from the item. The item should be an {@link Entry}.
     * 
     * @param item an {@link Entry}, that can have a value, that contains an
     * inner {@link Entry} and so on recursively.
     * @param parameterTypes contains required types for inspection of a result
     * and defines the length of a result.
     */
    public Object[] flattenOut(final Object item, List<Class<?>> parameterTypes) {
        if (item instanceof Entry<?, ?>) {
            Object[] result = new Object[parameterTypes.size()];
            Object innerObject = null;
            for (int i = 0; i < result.length - 1; i++) {
                if (innerObject == null) {
                    innerObject = item;
                }
                Entry<?, ?> entry = (Entry<?, ?>) innerObject;
                result[i] = entry.getKey();
                if (i == result.length - 2) {
                    result[i + 1] = entry.getValue();
                } else {
                    // TODO Zde je chyba. Je potreba tuto metodu predelat na rekurzivni a zpracovat vsechny polozky, ne firstItem.
                    innerObject = getFirstItem(entry.getValue());
                }
            }
            // inspection
            for (int i = 0; i < result.length; i++) {
                Class<?> nextType = parameterTypes.get(i);
                if (!nextType.isAssignableFrom(result[i].getClass())) {
                    throw new AppRuntimeException("Result is not assignable to the class. Result: " + result[i]
                            + ", class: " + nextType);
                }
            }
            return result;
        } else {
            throw new AppRuntimeException("Expected an Entry, byt found the " + item.getClass());
        }
    }

}
