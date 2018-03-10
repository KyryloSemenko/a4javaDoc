package com.apache.a4javadoc.javaagent.agent;

import static org.junit.Assert.*;

import org.junit.Test;

/** 
 * @author Kyrylo Semenko
 */
public class AgentTest {

    /**
     * This test should be run with parameters:<br>
     * <code>-javaagent:target\a4javadoc-javaagent-0.0.1-SNAPSHOT.jar -DincludePackages=com.apache.a4javadoc</code>
     */
    @Test
    public void test() {
        String stringParameter = "Parameter";
        int intParameter = 1;
        final String stringBuilderInitialContent = "StringBuilder parameter.";
        StringBuilder stringBuilder = new StringBuilder(stringBuilderInitialContent);
        method(stringParameter, intParameter, stringBuilder);
        assertTrue("StringBuilder is completed by a method.", !stringBuilder.toString().equals(stringBuilderInitialContent));
    }

    /** This method will be completed by {@link Agent} */
    private void method(String stringParameter, int intParameter, StringBuilder stringBuilder) {
        stringBuilder.append(" Completed by the method.");
    }

}
