package com.apache.a4javadoc.javaagent.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Root object of object graph should contain a class type, for example <pre>java.lang.String</pre><br>
 * Root object of object graph should contain a class type, for example <pre>int</pre><br>
 * Fields of JSON should contain a class type in case when the type cannot be obtained from a field when deserializing.
 * For example {@link List} or array of Objects where each item can have a different type. In general when the items contain different types.
 * TODO Kyrylo Semenko doplnit testy dle pravidel popsaných výše.
 * 
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
     * Test method for {@link GenericDeserializer} with circular dependency in JSON.
     */
    @Test
    public void testDeserializerCircular() {
        WrapperClass wrapperClass = new WrapperClass();
        wrapperClass.setId(1);
        wrapperClass.setParent(wrapperClass);
        
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, wrapperClass);
        
        String json = stringWriter.toString();
        
        System.out.println(json);
        
        Object object = objectMapperA4j.readValue(json, Object.class);
        
        assertTrue(object instanceof WrapperClass);
        WrapperClass deserializedObject = (WrapperClass) object;
        assertEquals(wrapperClass.getId(), deserializedObject.getId());
        assertEquals(deserializedObject, deserializedObject.getParent());
        assertEquals(wrapperClass.nullWithoutGetterAndSetter, deserializedObject.nullWithoutGetterAndSetter);
    }
    
    /**
     * Test method for {@link GenericDeserializer} with null value
     */
    @Test
    public void testNull() {
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, null);
        String json = stringWriter.toString();
        
        System.out.println(json);
        
        Object object = objectMapperA4j.readValue(json, Object.class);
        
        assertTrue(object == null);
    }
    
    /**
     * Test method for {@link GenericDeserializer} with list of strings
     */
    @Test
    public void testListOfStrings() {
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        Container container = new Container();
        container.setListOfStrings(Arrays.asList("a", "b"));
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, container);
        String json = stringWriter.toString();
        
        try {
            System.out.println(new ObjectMapper().writeValueAsString(container));
        } catch (JsonProcessingException e) {
            // TODO Kyrylo Semenko
            throw new AppRuntimeException(e);
        }
        
        System.out.println(json);
        
        Object object = objectMapperA4j.readValue(json, Object.class);
        
        assertEquals(container.getListOfStrings(), ((Container) object).getListOfStrings());
    }
    
    /**
     * Test method for {@link GenericDeserializer} string
     */
    @Test
    public void testString() {
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        Container container = new Container();
        container.setString("The string");
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, container);
        String json = stringWriter.toString();
        
        try {
            System.out.println(new ObjectMapper().writeValueAsString(container));
        } catch (JsonProcessingException e) {
            // TODO Kyrylo Semenko
            throw new AppRuntimeException(e);
        }
        
        System.out.println(json);
        
        Object object = objectMapperA4j.readValue(json, Object.class);
        
        assertEquals(container.getString(), ((Container) object).getString());
    }
    
    // TODO Kyrylo Semenko posbirat gettery a settery jako tady com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector._addMethods(Map<String, POJOPropertyBuilder>)

}
