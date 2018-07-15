package com.apache.a4javadoc.javaagent.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

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
    public void testDeserializeCircular() {
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
        try {
            ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
            ObjectMapper objectMapper = new ObjectMapper();
            Container container = new Container();
            container.setListOfStrings(Arrays.asList("a", "b"));
        
            long a4start = System.nanoTime();
            StringWriter stringWriter = new StringWriter();
            objectMapperA4j.writeValue(stringWriter, container);
            String json = stringWriter.toString();
            System.out.println(json);
            long a4time = System.nanoTime() - a4start;
            
            long objectMapperStart = System.nanoTime();
            StringWriter stringWriterOM = new StringWriter();
            objectMapper.writeValue(stringWriterOM, container);
            System.out.println(stringWriterOM);
            long objectMapperTime = System.nanoTime() - objectMapperStart;
            
            System.out.println("a4time in milliseconds: " + a4time/1000);
            System.out.println("OMtime in milliseconds: " + objectMapperTime/1000);
            
            Object object = objectMapperA4j.readValue(json, Object.class);
            
            assertEquals(container.getListOfStrings(), ((Container) object).getListOfStrings());
            
            assertTrue(EqualsBuilder.reflectionEquals(container, object));
        } catch (Exception e) {
            // TODO Kyrylo Semenko
            throw new AppRuntimeException(e);
        }
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
    
    /** Test of var args serialization and deserialization, see {@link VarArgsClass#callMethod(String, int...)} */
    @Test
    public void TestVarArgs() {
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        VarArgsClass varArgClass = new VarArgsClass();
        varArgClass.callMethod("stringValue", 1, 2, 3);
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, varArgClass);
        String json = stringWriter.toString();
        System.out.println(json);
        
        VarArgsClass deserialized = (VarArgsClass) objectMapperA4j.readValue(json);
        
        assertEquals(0, CompareToBuilder.reflectionCompare(varArgClass, deserialized));
    }
    
    /** Test of serialization and deserialization of an object with a Map */
    @Test
    public void TestMap() {
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        Map<Integer, String> map = new TreeMap<>();
        map.put(1, "one");
        map.put(2, "two");
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, map);
        String json = stringWriter.toString();
        System.out.println(json);
        
        @SuppressWarnings("unchecked")
        Map<Integer, String> deserialized = (Map<Integer, String>) objectMapperA4j.readValue(json);
        
        assertEquals(0, CompareToBuilder.reflectionCompare(map, deserialized));
        
        assertTrue(map.size() == deserialized.size());

        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            assertNotNull(deserialized.entrySet().contains(entry));
        }
    }
    
    /** Test of serialization and deserialization of an object with a Map */
    @Test
    public void TestMapWithComplexKeysAndValues() {
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        Map<Object, Container> map = new TreeMap<>();
        Container container1 = new Container();
        container1.setString("container1");
        
        Container container2 = new Container();
        container2.setString("container2");
        
        Container container3 = new Container();
        container3.setString("container3");
        container3.setObjectField(container1);
        
        Container container4 = new Container();
        container4.setString("container4");
        container4.setObjectField(container3);
        
        map.put(container1, container3);
        map.put(container2, container4);
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, map);
        String json = stringWriter.toString();
        System.out.println(json);
        
        @SuppressWarnings("unchecked")
        Map<Object, Container> deserialized = (Map<Object, Container>) objectMapperA4j.readValue(json);
        
        assertEquals(0, CompareToBuilder.reflectionCompare(map, deserialized));
        
        assertTrue(map.size() == deserialized.size());
        
        for (Map.Entry<Object, Container> entry : map.entrySet()) {
            assertNotNull(deserialized.entrySet().contains(entry));
        }
    }
    
    /** Test of serialization and deserialization of an multidimensional array */
    @Test
    public void TestMultiDimensionalArray() {
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        Object[][][] array = new Integer[2][2][3];
        for (int i = 0; i < array[0].length; i++) {
            for (int k = 0; k < array[i].length; k++) {
                for (int t = 0; t < array[k].length; t++) {
                    array[i][k][t] = i + k + t + 1;
                }
            }
        }
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, array);
        String json = stringWriter.toString();
        System.out.println(json);
        
        Object[][][] deserialized = (Object[][][]) objectMapperA4j.readValue(json);
        
        assertEquals(0, CompareToBuilder.reflectionCompare(array, deserialized));
        for (int i = 0; i < array[0].length; i++) {
            for (int k = 0; k < array[1].length; k++) {
                for (int t = 0; t < array[k].length; t++) {
                    assertEquals(array[i][k][t], deserialized[i][k][t]);
                }
            }
        }
    }
    
    /** Test of serialization and deserialization of an array */
    @Test
    public void TestArray() {
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        Object[] array = new Integer[2];
        for (int i = 0; i < array.length; i++) {
            array[i] = i+1;
        }
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, array);
        String json = stringWriter.toString();
        System.out.println(json);
        
        Object deserialized = objectMapperA4j.readValue(json);
        
        assertEquals(0, CompareToBuilder.reflectionCompare(array, deserialized));
    }
    
    /** Test of serialization and deserialization of an multidimensional array */
    @Test
    public void Test3DimensionalTable() {
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        Container container = new Container();
        Table<String, String, Integer> table = HashBasedTable.create();
        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 2; k++) {
                table.put("a" + i, "b" + k, i + k);
            }
        }
        container.setObjectField(table);
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, container);
        String json = stringWriter.toString();
        System.out.println(json);
        
        Object deserialized = objectMapperA4j.readValue(json);
        
        assertEquals(0, CompareToBuilder.reflectionCompare(container, deserialized));
    }
    
    /** Test of serialization and deserialization when the same object presents in a list multiple times */
    @Test
    public void TestDuplicatesInList() {
        Container container = new Container();
        container.setObjectField(container);
        String string = "the same container";
        container.setString(string);
        List<?> list = Arrays.asList("String 1", container, "String 2", container);
        
        ObjectMapperA4j objectMapperA4j = ObjectMapperA4j.getInstance();
        StringWriter stringWriter = new StringWriter();
        objectMapperA4j.writeValue(stringWriter, list);
        String json = stringWriter.toString();
        System.out.println(json);
        
        // the string should be contained only once in the json
        assertTrue(json.split(string).length == 2);
        
        List<?> deserialized = (List<?>) objectMapperA4j.readValue(json);
        
        assertEquals(0, CompareToBuilder.reflectionCompare(list, deserialized));
    }
    
}
