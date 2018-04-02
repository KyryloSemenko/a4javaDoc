package com.apache.a4javadoc.javaagent.agent.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/** 
 * @author Kyrylo Semenko
 */
public class AgentTest {
    
    private static final String STRING_BUILDER_PARAMETER = "StringBuilder parameter.";
    private List<String> localField = new ArrayList<String>(); 

    /**
     * This test should be started with parameters:<br>
     * <code>-javaagent:target\a4javadoc-javaagent-0.0.1-SNAPSHOT.jar -DincludePackages=com.apache.a4javadoc</code>
     */
    @Test
    public void test() throws InterruptedException {
        Thread thread = new Thread(new ThreadExample());
        thread.start();
        for (int i = 0; i < 5; i++) {
            String stringParameter = "Parameter";
            int intParameter = 1;
            final String stringBuilderInitialContent = STRING_BUILDER_PARAMETER;
            localField.add(STRING_BUILDER_PARAMETER);
            StringBuilder stringBuilder = new StringBuilder(stringBuilderInitialContent);
            boolean result = method(stringParameter, intParameter, stringBuilder);
            localField.add("Result: " + result);
            assertTrue("StringBuilder is completed by a method.", !stringBuilder.toString().equals(stringBuilderInitialContent));
//            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println("The end");
    }

    /** This method will be completed by {@link Agent} */
    private boolean method(String stringParameter, int intParameter, StringBuilder stringBuilder) {
        stringBuilder.append(" Completed by the method.");
        localField.add(stringBuilder.toString());
        return true;
    }

}
