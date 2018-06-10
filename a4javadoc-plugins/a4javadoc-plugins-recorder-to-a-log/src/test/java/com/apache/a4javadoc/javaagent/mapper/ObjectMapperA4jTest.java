package com.apache.a4javadoc.javaagent.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.junit.Test;

/** 
 * @author Kyrylo Semenko
 */
public class ObjectMapperA4jTest {

    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.mapper.ObjectMapperA4j#getInstance()}.
     */
    @Test
    public void testGetInstance() {
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        assertNotNull(objectMapperA4j);
    }

    /**
     * TODO Kyrylo Semenko
     */
    @Test
    public void testDeserializer() {
        WrapperClass wrap1 = new WrapperClass();
        wrap1.setId(1);
        wrap1.setParent(wrap1);
        
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, wrap1);
        
        String json = stringWriter.toString();
        
        Object object = objectMapperA4j.readValue(json, Object.class);
        
        assertTrue(object instanceof WrapperClass);
        WrapperClass deserializedObject = (WrapperClass) object;
        assertEquals(wrap1.getId(), deserializedObject.getId());
        assertEquals(deserializedObject, deserializedObject.getParent());
    }
    
    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.mapper.ObjectMapperA4j#writeValue(java.io.Writer, java.lang.Object)}.
     */
    @Test
    public void testWriteWrapped() {
        WrapperClass wrap1 = new WrapperClass();
        wrap1.setId(1);
        wrap1.setParent(wrap1);
        
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, wrap1);
        /*
         * Expected value
            {
               "_a4id":"com.apache.a4javadoc.javaagent.mapper.WrapperClass@336eaa9e",
               "id":"1",
               "parent":{
                  "_a4id":"com.apache.a4javadoc.javaagent.mapper.WrapperClass@336eaa9e"
               }
            }
         */
        System.out.println(stringWriter.toString());
        String expected = "{\""
        + GenericSerializer.GENERIC_KEY_ID
        + "\":\""
        + wrap1.getClass().getName() + GenericSerializer.GENERIC_VALUE_SEPARATOR + Integer.toHexString(wrap1.hashCode())
        + "\",\"id\":\"1\",\"parent\":{\""
        + GenericSerializer.GENERIC_KEY_ID
        + "\":\""
        + wrap1.getClass().getName() + GenericSerializer.GENERIC_VALUE_SEPARATOR + Integer.toHexString(wrap1.hashCode())
        + "\"}}";
        
        assertEquals(expected, stringWriter.toString());
    }

}
