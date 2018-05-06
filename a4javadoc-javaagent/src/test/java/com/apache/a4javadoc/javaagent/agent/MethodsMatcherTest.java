package com.apache.a4javadoc.javaagent.agent;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.apache.a4javadoc.javaagent.agent.namefilter.NameFilterService;
import com.apache.a4javadoc.javaagent.test.TestService;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;

/** 
 * @author Kyrylo Semenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodsMatcherTest {
    
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
    public void after() {
        TestService.setMockInstance(null, NameFilterService.class, "instance");
    }
    
    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.MethodsMatcher#matches(net.bytebuddy.description.method.MethodDescription)}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testMatches() {
        when(nameFilterService.matches(any(String.class))).thenReturn(true);
        
        MethodsMatcher<? super MethodDescription> methodsMatcher = MethodsMatcher.getInstance();
        
        InDefinedShape.AbstractBase target = mock(InDefinedShape.AbstractBase.class);
        
        TypeDescription typeDeskription = mock(TypeDescription.class);
        when(target.getDeclaringType()).thenReturn((TypeDescription) typeDeskription);
        
        when(typeDeskription.getTypeName()).thenReturn("java.lang.String");
        when(target.getInternalName()).thenReturn("toString");
        ParameterDescription.AbstractBase parameterFirst = mock(ParameterDescription.AbstractBase.class);
        ParameterDescription.AbstractBase parameterSecond = mock(ParameterDescription.AbstractBase.class);
        
        Generic generic = mock(Generic.class);
        when(generic.getTypeName()).thenReturn("int");
        when(parameterFirst.getType()).thenReturn(generic);
        when(parameterSecond.getType()).thenReturn(generic);
        ParameterList.AbstractBase parameterList = mock(ParameterList.AbstractBase.class);
        when(parameterList.size()).thenReturn(2);
        when(parameterList.get(0)).thenReturn(parameterFirst);
        when(parameterList.get(1)).thenReturn(parameterSecond);
        
        when(target.getParameters()).thenReturn(parameterList);
        
        boolean result = methodsMatcher.matches(target);
        
        assertTrue("Result should be true, because mocked NameFilterService defined", result);
        verify(nameFilterService).matches(any(String.class));
    }

}
