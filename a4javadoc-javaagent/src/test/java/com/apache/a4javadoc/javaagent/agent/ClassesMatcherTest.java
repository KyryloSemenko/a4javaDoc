package com.apache.a4javadoc.javaagent.agent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.apache.a4javadoc.javaagent.agent.namefilter.NameFilterService;
import com.apache.a4javadoc.javaagent.test.TestService;

import net.bytebuddy.description.type.TypeDescription;

/** 
 * @author Kyrylo Semenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassesMatcherTest {
    
    @Spy
    NameFilterService nameFilterService = NameFilterService.getInstance();
    
    /**
     * Set the mocked instance
     */
    @Before
    public void before() {
        TestService.setMockInstance(nameFilterService, NameFilterService.class, "instance");
    }
    
    /**
     * Remove the mocked instance
     */
    @After
    public void resetSingleton() {
        TestService.setMockInstance(nameFilterService, NameFilterService.class, "instance");
    }

    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.ClassesMatcher#matches(net.bytebuddy.description.type.TypeDescription, java.lang.ClassLoader, net.bytebuddy.utility.JavaModule, java.lang.Class, java.security.ProtectionDomain)}.
     */
    @Test
    public void testNotMatches() {
        TypeDescription typeDescription = mock(TypeDescription.class);
        when(typeDescription.getName()).thenReturn("");
        boolean result = new ClassesMatcher().matches(typeDescription, null, null, null, null);
        assertFalse("result should be false, because no 'includes' filter defined", result);
    }
    
    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.ClassesMatcher#matches(net.bytebuddy.description.type.TypeDescription, java.lang.ClassLoader, net.bytebuddy.utility.JavaModule, java.lang.Class, java.security.ProtectionDomain)}.
     */
    @Test
    public void testMatches() {
        when(nameFilterService.matches((String) any())).thenReturn(true);

        TypeDescription typeDescription = mock(TypeDescription.class);
        when(typeDescription.getName()).thenReturn("");
        
        boolean result = new ClassesMatcher().matches(typeDescription, null, null, null, null);
        assertTrue("result should be true, because mock returned true", result);
        
        
        when(nameFilterService.matches((String) any())).thenReturn(false);
        
        boolean resultFalse = new ClassesMatcher().matches(typeDescription, null, null, null, null);
        assertFalse("result should be false, because mock returned true", resultFalse);
        
        
    }

}
