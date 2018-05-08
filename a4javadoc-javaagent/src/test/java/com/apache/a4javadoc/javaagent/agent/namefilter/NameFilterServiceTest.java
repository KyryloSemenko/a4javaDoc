package com.apache.a4javadoc.javaagent.agent.namefilter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.apache.a4javadoc.javaagent.parameter.ParameterService;
import com.apache.a4javadoc.javaagent.test.TestService;

/** 
 * @author Kyrylo Semenko
 */
@RunWith(MockitoJUnitRunner.class)
public class NameFilterServiceTest {
    
    @Mock
    ParameterService parameterService = ParameterService.getInstance();
    
    /**
     * Set the mocked instance
     */
    @Before
    public void before() {
        TestService.setMockInstance(parameterService, ParameterService.class, "instance");
    }
    
    /**
     * Clean up after a test
     */
    @After
    public void after() {
        TestService.setMockInstance(null, ParameterService.class, "instance");
        TestService.setMockInstance(null, NameFilterService.class, "instance");
    }

    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.namefilter.NameFilterService#matches(java.lang.String)}.<br>
     * See description in {@link NameFilterService#PROPERTY_INCLUDE_NAMES} and {@link NameFilterService#PROPERTY_EXCLUDE_NAMES}.
     */
    @Test
    public void testMatches() {
        when(parameterService.getProperty(NameFilterService.PROPERTY_INCLUDE_NAMES)).thenReturn("com.foo.*");
        when(parameterService.getProperty(NameFilterService.PROPERTY_EXCLUDE_NAMES)).thenReturn("*secure*");
        NameFilterService nameFilterService = NameFilterService.getInstance();
        assertTrue("The name should be included", nameFilterService.matches("com.foo.ExampleClass.method(java.lang.String)"));
        assertFalse("The name should be excluded", nameFilterService.matches("com.foo.secure.Anything.method()"));
    }
    
    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.namefilter.NameFilterService#matches(java.lang.String)}.<br>
     * See description in {@link NameFilterService#PROPERTY_INCLUDE_NAMES} and {@link NameFilterService#PROPERTY_EXCLUDE_NAMES}.
     */
    @Test
    public void testMatchesEmpty() {
        when(parameterService.getProperty(NameFilterService.PROPERTY_INCLUDE_NAMES)).thenReturn("");
        when(parameterService.getProperty(NameFilterService.PROPERTY_EXCLUDE_NAMES)).thenReturn("");
        NameFilterService nameFilterService = NameFilterService.getInstance();
        assertFalse("The name should be excluded", nameFilterService.matches("com.foo.ExampleClass.method(java.lang.String)"));
        assertFalse("The name should be excluded", nameFilterService.matches("com.foo.secure.Anything.method()"));
    }

}
