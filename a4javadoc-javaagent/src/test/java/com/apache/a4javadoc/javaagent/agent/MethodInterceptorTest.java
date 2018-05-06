package com.apache.a4javadoc.javaagent.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** 
 * @author Kyrylo Semenko
 */
public class MethodInterceptorTest {
    
    @SuppressWarnings("javadoc")
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.MethodInterceptor#enter(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])}.
     */
    @Test
    public void testEnter() {
        Object[] allArguments = new Object[0];
        Long methodInvocationId = MethodInterceptor.enter("methodName", "declaringType", "methodDescriptor", "methodSignature", "returnType", "methodComplexName", allArguments);
        assertTrue("The first methodInvocationId should be 1", methodInvocationId == 1);
    }

    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.MethodInterceptor#exit(long, java.lang.String, java.lang.Object, java.lang.Throwable, java.lang.Object[])}.
     */
    @Test
    public void testExit() {
        Object[] allArguments = new Object[0];
        MethodInterceptor.exit(2L, "methodComplexName", null, null, allArguments);
    }

    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.MethodInterceptor#toString(java.lang.Object[])}.
     */
    @Test
    public void testToStringObjectArray() {
        String[] allArguments = new String[]{"one", "two"};
        String result = MethodInterceptor.toString(allArguments);
        assertEquals("{one, two}", result);
    }
    
    /**
     * Test method for {@link MethodInterceptor#MethodInterceptor()}
     */
    @Test
    public void testMethodInterceptor() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(MethodInterceptor.SHOULD_NOT_BE_INSTANTIATED);
        new MethodInterceptor();
    }

}
