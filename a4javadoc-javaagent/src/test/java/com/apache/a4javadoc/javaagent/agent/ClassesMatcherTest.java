package com.apache.a4javadoc.javaagent.agent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.apache.a4javadoc.javaagent.agent.namefilter.NameFilterService;

import net.bytebuddy.description.type.TypeDescription;

/** 
 * @author Kyrylo Semenko
 */
public class ClassesMatcherTest {

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
        NameFilterService nameFilterService = mock(NameFilterService.class);
        when(nameFilterService.matches((String) any())).thenReturn(true);
        NameFilterService.setMockInstance(nameFilterService); 
        TypeDescription typeDescription = mock(TypeDescription.class);
        when(typeDescription.getName()).thenReturn("");
        boolean result = new ClassesMatcher().matches(typeDescription, null, null, null, null);
        assertTrue("result should be true, because mock returned true", result);
    }

}
