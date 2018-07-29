package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.apache.a4javadoc.exception.AppRuntimeException;

/**
 * Stateless singleton for working with java {@link Type}s.
 * @author Kyrylo Semenko
 */
public class TypeService {
    
    private static final String EMPTY_STRING = "";
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
     * Generate a generic signature of a value, for example {@code <int,java.lang.String>}.
     * Process {@link Iterable}s and {@link Map}s only.
     * @param value object instance as a source of the signature
     * @return an empty string if the value is not {@link Iterable} nor {@link Map}.
     */
    public String findOutGenericTypesSignature(Object value) {
        Class<?> clazz = value.getClass();

        if (Iterable.class.isAssignableFrom(clazz)) {
            Iterable<?> iterable = (Iterable<?>) value;
            Iterator<?> iterator = iterable.iterator();
            Class<?> commonClass = null;
            while (iterator.hasNext()) {
                Object object = iterator.next();
                Class<?> objectClass = object.getClass();
                commonClass = ClassService.getInstance().findCommonParent(commonClass, objectClass);
            }
            if (commonClass == null) {
                throw new AppRuntimeException("closestClass cannot be null");
            }
            return IdentifierService.GENERIC_LEFT_BRACKET + commonClass.getCanonicalName() + IdentifierService.GENERIC_RIGHT_BRACKET;
        }
        if (Map.class.isAssignableFrom(clazz)) {
            Map<?, ?> map = (Map<?, ?>) value;
            Class<?> keyCommonClass = null;
            Class<?> valueCommonClass = null;
            for (Object object : map.entrySet()) {
                Entry<?, ?> entry = (Entry<?, ?>) object;
                // key
                Class<?> keyClass = entry.getKey().getClass();
                keyCommonClass = ClassService.getInstance().findCommonParent(keyCommonClass, keyClass);
                // value
                Class<?> valueClass = entry.getValue().getClass();
                valueCommonClass = ClassService.getInstance().findCommonParent(valueCommonClass, valueClass);
            }
            return IdentifierService.GENERIC_LEFT_BRACKET + keyCommonClass.getCanonicalName() + IdentifierService.GENERIC_COMMA + valueCommonClass.getCanonicalName() + IdentifierService.GENERIC_RIGHT_BRACKET;
         }
        return EMPTY_STRING;
    }

//    /** TODO */
//    private Class<?> findCommonClass(Class<?> commonClass, Class<?> clazz) {
//        if (commonClass == null) {
//            commonClass = clazz;
//        } else {
//            if (!commonClass.isAssignableFrom(clazz)) {
//                commonClass = clazz;
//            }
//        }
//        if (commonClass == null) {
//            throw new AppRuntimeException("commonClass cannot be null");
//        }
//        return commonClass;
//    }

}
