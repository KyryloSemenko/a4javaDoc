package com.apache.a4javadoc.javaagent.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/** 
 * Tests of the {@link IdentifierService#createIdentifier(Object)} method.
 * @author Kyrylo Semenko
 */
public class IdentifierServiceTest {
    
    /**
     * Test of an {@link Array}
     */
    @Test
    public void testCreateIdentifierOfArray() {
        ConfigService.getInstance().setMaxDepth(3);
        Object[] array = new Object[]{"a", "b"};
        Identifier identifier = IdentifierService.getInstance().createIdentifier(array);
        
        assertNotNull(identifier.getContainerType());
        // List 1
        assertEquals("[Ljava.lang.Object;",
                identifier.getContainerType().getObjectClass().getName());
        assertEquals(1,
                identifier.getContainerType().getContainerTypes().size());
        // List 2
        assertEquals(String.class.getName(),
                identifier.getContainerType().getContainerTypes().get(0).getObjectClass().getName());
        assertEquals(0,
                identifier.getContainerType().getContainerTypes().get(0).getContainerTypes().size());
    }
    
    /**
     * Test of a {@link List} of {@link List}s.
     */
    @Test
    public void testCreateIdentifierListOfLists() {
        ConfigService.getInstance().setMaxDepth(3);
        List<List<String>> listOfLists = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            listOfLists.add(Arrays.asList(Integer.toString(i), "a", "b"));
        }
        Identifier identifier = IdentifierService.getInstance().createIdentifier(listOfLists);

        assertNotNull(identifier.getContainerType());
        // List 1
        assertEquals(ArrayList.class.getName(),
                identifier.getContainerType().getObjectClass().getName());
        assertEquals(1,
                identifier.getContainerType().getContainerTypes().size());
        // List 2
        ContainerType firstInnerContainerType = identifier.getContainerType().getContainerTypes().get(0);
        
        assertEquals("java.util.Arrays$ArrayList",
                firstInnerContainerType.getObjectClass().getName());
        assertEquals(1,
                firstInnerContainerType.getContainerTypes().size());
        // String
        assertEquals(String.class.getName(),
                firstInnerContainerType.getContainerTypes().get(0).getObjectClass().getName());
        assertEquals(0,
                firstInnerContainerType.getContainerTypes().get(0).getContainerTypes().size());
    }
    
    /**
     * Test of the {@link IdentifierService#createIdentifier(Object)} method.
     */
    @Test
    public void testCreateIdentifierListOfListsWithDifferentObjectTypes() {
        ConfigService.getInstance().setMaxDepth(3);
        List<Object> listOfLists = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            listOfLists.add(Arrays.asList(i, "a", "b"));
        }
        Identifier identifier = IdentifierService.getInstance().createIdentifier(listOfLists);

        assertNotNull(identifier.getContainerType());
        // depth 1
        assertEquals(ArrayList.class.getName(),
                identifier.getContainerType().getObjectClass().getName());
        assertEquals(1,
                identifier.getContainerType().getContainerTypes().size());
        // depth 2
        ContainerType firstInnerContainerType = identifier.getContainerType().getContainerTypes().get(0);
        assertEquals("java.util.Arrays$ArrayList",
                firstInnerContainerType.getObjectClass().getName());
        assertEquals(1,
                firstInnerContainerType.getContainerTypes().size());
        // depth 3
        assertEquals(Object.class.getName(),
                firstInnerContainerType.getContainerTypes().get(0).getObjectClass().getName());
    }
    
    /**
     * Test of the {@link IdentifierService#createIdentifier(Object)} method.
     * The third inner list should not be included to {@link Identifier}
     */
    @Test
    public void testCreateIdentifierOfThreeInnerLists() {
        List<Object> outerList = new LinkedList<>();
        List<List<String>> listOfLists = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            listOfLists.add(Arrays.asList(Integer.toString(i), "a", "b"));
        }
        outerList.add(listOfLists);
        outerList.add(listOfLists);
        
        // Root identifier
        ConfigService.getInstance().setMaxDepth(3);
        Identifier identifier = IdentifierService.getInstance().createIdentifier(outerList);
        
        // List depth 1
        assertNotNull(identifier.getContainerType());
        assertEquals(LinkedList.class.getName(),
                identifier.getContainerType().getObjectClass().getName());
        assertEquals(1,
                identifier.getContainerType().getContainerTypes().size());
        
        // List depth 2
        ContainerType firstInnerContainerType = identifier.getContainerType().getContainerTypes().get(0);
        assertEquals(ArrayList.class.getName(),
                firstInnerContainerType.getObjectClass().getName());
        assertEquals(1,
                firstInnerContainerType.getContainerTypes().size());

        // List depth 2
        assertEquals("java.util.Arrays$ArrayList",
                firstInnerContainerType.getContainerTypes().get(0).getObjectClass().getName());
        // Only three levels allowed
        assertTrue(
                firstInnerContainerType.getContainerTypes().get(0).getContainerTypes().isEmpty());
    }
    
    /**
     * Test of the {@link IdentifierService#createIdentifier(Object)} method.
     * The third inner list should be included to {@link Identifier}
     * <pre>
Identifier{
    hash=792d7d45,
    containerType=ContainerType{
        className=java.util.HashMap, // (0) outerMap, depth 1
        containerTypes={
            ContainerType{
                className=java.util.Arrays$ArrayList, // (0.0) outerMap key middleList, depth 2
                containerTypes={
                    ContainerType{
                        className=java.lang.String, // (0.0.0) outerMap key - middleList value, depth 3
                        containerTypes={}
                    }
                }
            },
            ContainerType{
                className=java.util.HashMap, // (0.1) outerMap value, depth 2
                containerTypes={
                    ContainerType{
                        className=java.lang.String, // (0.1.0) outerMap value - middleMap key - String depth 3
                        containerTypes={}
                    },
                    ContainerType{
                        className=java.util.ArrayList, // (0.1.1) outerMap value - middleMap value - innerList, depth 3
                        containerTypes={
                            ContainerType{                  // (0.1.1.0) This node should not be included, because max_depth is 3 and this node has depth 4.
                                className=java.lang.String,
                                containerTypes={}
                            }
                        }
                    }
                }
            }
        }
    }
}
     * </pre>
     */
    @Test
    public void testCreateIdentifierOfMapWithListAndMapOfLists() {
        // outerMap<middleList<Arrays$ArrayList<String>>, middleMap<String, innerList<String>>>
        Map<List<Object>, Map<Object, List<Object>>> outerMap = new HashMap<>();
        
        List<Object> middleList = Arrays.asList((Object) "middle", (Object) "list");
        
        Map<Object, List<Object>> middleMap = new HashMap<>();
        
        List<Object> innerList = new ArrayList<>();
        innerList.add("innerList item");
        
        middleMap.put("middleMap key 1", innerList);
        
        outerMap.put(middleList, middleMap);
        
        // Root identifier
        ConfigService.getInstance().setMaxDepth(3);
        Identifier identifier = IdentifierService.getInstance().createIdentifier(outerMap);
        
        // (0) outerMap depth 1
        assertNotNull(identifier.getContainerType());
        assertEquals(outerMap.getClass().getName(),
                identifier.getContainerType().getObjectClass().getName());
        assertEquals(2,
                identifier.getContainerType().getContainerTypes().size());
        
        // (0.0) outerMap key - middleList, depth 2
        ContainerType firstInnerContainerType = identifier.getContainerType().getContainerTypes().get(0);
        assertEquals(middleList.getClass().getName(),
                firstInnerContainerType.getObjectClass().getName());
        assertEquals(1,
                firstInnerContainerType.getContainerTypes().size());
        
        // (0.0.0) outerMap key - middleList value, depth 3
        assertEquals(String.class.getName(),
                firstInnerContainerType.getContainerTypes().get(0).getObjectClass().getName());
        assertEquals(0,
                firstInnerContainerType.getContainerTypes().get(0).getContainerTypes().size());
        
        // (0.1) outerMap value - middle map, depth 2
        ContainerType secondInnerContainerType = identifier.getContainerType().getContainerTypes().get(1);
        assertEquals(middleMap.getClass().getName(),
                secondInnerContainerType.getObjectClass().getName());
        assertEquals(2,
                secondInnerContainerType.getContainerTypes().size());
        
        // (0.1.0) outerMap value - middleMap key - String depth 3
        assertEquals(String.class.getName(),
                secondInnerContainerType.getContainerTypes().get(0).getObjectClass().getName());
        assertEquals(0,
                secondInnerContainerType.getContainerTypes().get(0).getContainerTypes().size());
        
        // (0.1.1) outerMap value - middleMap value - innerList, depth 3
        assertEquals(innerList.getClass().getName(),
                secondInnerContainerType.getContainerTypes().get(1).getObjectClass().getName());
        assertEquals(0,
                secondInnerContainerType.getContainerTypes().get(1).getContainerTypes().size());
    }
    
}
