package com.apache.a4javadoc.javaagent.agent.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.javaagent.agent.Agent;

/** 
 * @author Kyrylo Semenko
 */
public class AgentTest {
    private static final Logger logger = LoggerFactory.getLogger(AgentTest.class);
    
    private static final String STRING_BUILDER_PARAMETER = "StringBuilder parameter.";
    private List<String> localField = new ArrayList<String>(); 

    /**
     * This test should be started with parameters:<br>
     * <code>-javaagent:target\a4javadoc-javaagent-0.0.1-SNAPSHOT.jar -Da4javadoc.include=*a4javadoc*test*</code>
     */
    @Test
    public void test() throws InterruptedException {
        Thread thread = new Thread(new ThreadExample());
        thread.start();
        for (int i = 0; i < 1; i++) {
            String stringParameter = "Parameter";
            int intParameter = 1;
            final String stringBuilderInitialContent = STRING_BUILDER_PARAMETER;
            localField.add(STRING_BUILDER_PARAMETER);
            StringBuilder stringBuilder = new StringBuilder(stringBuilderInitialContent);
            boolean result = method(stringParameter, intParameter, stringBuilder);
            localField.add("Result: " + result);
            assertTrue("StringBuilder is completed by a method.", !stringBuilder.toString().equals(stringBuilderInitialContent));
        }
        AgentTest.staticMethod(thread.getName());
        logger.info("The end");
//        throw new RuntimeException("Exception message");
    }

    private static void staticMethod(String name) {
        logger.info("Thread name: {}", name);
    }

    /** This method will be completed by {@link Agent} */
    private boolean method(String stringParameter, int intParameter, StringBuilder stringBuilder) {
        stringBuilder.append(" Completed by the method.");
        localField.add(stringBuilder.toString());
        return true;
    }

}
