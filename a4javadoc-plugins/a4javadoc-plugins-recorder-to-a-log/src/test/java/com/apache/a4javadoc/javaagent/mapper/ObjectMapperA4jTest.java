package com.apache.a4javadoc.javaagent.mapper;

import static org.junit.Assert.assertNotNull;

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
     * Test method for {@link com.apache.a4javadoc.javaagent.mapper.ObjectMapperA4j#writeValue(java.io.Writer, java.lang.Object)}.
     */
    @Test
    public void testWriteValueCircular() {
        CircularClass child = new CircularClass();
        child.setId(2);
        child.setParent(child);
        
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, child);
        assertNotNull(stringWriter);
    }

}
