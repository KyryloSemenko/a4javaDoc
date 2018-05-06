package com.apache.a4javadoc.javaagent.test;

import java.lang.reflect.Field;

/** 
 * Static helper for test purposes.
 * @author Kyrylo Semenko
 */
public class TestService {

    /**
     * Set a mock or the null as a value of fieldName of a targetClass for test purposes.
     * @param mock a mocked object or null that will be set to a targetClass
     * @param targetClass the target of injection
     * @param fieldName a field in a targetClass. It should not be final
     */
    public static void setMockInstance(Object mock, Class<?> targetClass, String fieldName) {
        try {
            Field instance = targetClass.getDeclaredField(fieldName);
            instance.setAccessible(true);
            instance.set(instance, mock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
