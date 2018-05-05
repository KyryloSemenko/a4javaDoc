package com.apache.a4javadoc.javaagent.agent.namefilter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

/** 
 * @author Kyrylo Semenko
 */
public class NameFilterServiceTest {

    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.namefilter.NameFilterService#matches(java.lang.String)}.<br>
     * See description in {@link NameFilterService#SYSTEM_PROPERTY_INCLUDE_NAMES} and {@link NameFilterService#SYSTEM_PROPERTY_EXCLUDE_NAMES}.
     */
    @Test
    public void testMatches() {
        NameFilterService nameFilterService = NameFilterService.getInstance();
        nameFilterService.includeNames = new HashSet<String>(Arrays.asList("com.foo.*"));
        nameFilterService.excludeNames = new HashSet<String>(Arrays.asList("*secure*"));
        assertTrue("The name should be included", nameFilterService.matches("com.foo.ExampleClass.method(java.lang.String)"));
        assertFalse("The name should be excluded", nameFilterService.matches("com.foo.secure.Anything.method()"));
    }

}
