package com.apache.a4javadoc.javaagent.recorder;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.javaagent.api.MethodStateContainer;
import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;
import com.apache.a4javadoc.javaagent.mapper.ObjectMapperA4j;

/**
 * Implementation of {@link MethodStateRecorder} methods. Saves recorded data to an application logger.
 * @author Kyrylo Semenko
 */
@Extension
public class MethodStateToLogFileRecorder implements MethodStateRecorder {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodStateToLogFileRecorder.class);
    
    /** Constructor */
    public MethodStateToLogFileRecorder() {
        logger.info("Recorder constructed");
    }
    
    @Override
    public void recordBefore(MethodStateContainer methodStateContainer) {
        List<Entry<String, Object>> list = generateStateMap(methodStateContainer);
        StringWriter stringWriter = new StringWriter().append("State before: ");
        ObjectMapperA4j.getInstance().writeValue(stringWriter, list);
        if (logger.isInfoEnabled()) {
            logger.info(stringWriter.toString());
        }
    }
    
    @Override
    public void recordThrowable(MethodStateContainer methodStateContainer, Exception exception) {
        List<Entry<String, Object>> list = generateStateMap(methodStateContainer);
        list.add(new AbstractMap.SimpleEntry<>("exception", (Object) exception.getMessage()));
        StringWriter stringWriter = new StringWriter().append("Exception state: ");
        ObjectMapperA4j.getInstance().writeValue(stringWriter, list);
        if (logger.isInfoEnabled()) {
            logger.info(stringWriter.toString());
        }
    }
    
    @Override
    public void recordAfter(MethodStateContainer methodStateContainer, Object result) {
        List<Entry<String, Object>> list = generateStateMap(methodStateContainer);
        Object generatedResult = generateResult(methodStateContainer.getInstrumentalizedMethod(), result);
        list.add(new AbstractMap.SimpleEntry<>("result", generatedResult));
        StringWriter stringWriter = new StringWriter().append("State after: ");
        ObjectMapperA4j.getInstance().writeValue(stringWriter, list);
        if (logger.isInfoEnabled()) {
            logger.info(stringWriter.toString());
        }
    }
    
    /**
     * @param instrumentalizedMethod contains a type
     * @param result contains a value
     * @return for example:
     * <pre>boolean:true</pre>
     * <pre>void:null</pre>
     */
    private Entry<String, String> generateResult(Method instrumentalizedMethod, Object result) {
        return new AbstractMap.SimpleEntry<>(instrumentalizedMethod.getReturnType().getName(), String.valueOf(result));
    }

    /**
     * Prepare data for a JSON string
     * @param methodStateContainer the data source
     */
    private List<Entry<String, Object>> generateStateMap(MethodStateContainer methodStateContainer) {
        List<Entry<String, Object>> list = new ArrayList<>();
        
        AbstractMap.SimpleEntry<String, Object> idEntry = new AbstractMap.SimpleEntry<>("methodInvocationId", (Object) methodStateContainer.getMethodInvocationId());
        list.add(idEntry);

        String caller = findCaller(methodStateContainer.getStackTrace());
        list.add(new AbstractMap.SimpleEntry<String, Object>("caller", caller));
        
        String nameEntry = generateMethodName(methodStateContainer.getInstrumentalizedObject(), methodStateContainer.getInstrumentalizedMethod());
        list.add(new AbstractMap.SimpleEntry<String, Object>("name", nameEntry));
        
        List<Entry<String, Object>> parameters = generateParameters(methodStateContainer.getInstrumentalizedMethod(), methodStateContainer.getInstrumentalizedParameters());
        list.add(new AbstractMap.SimpleEntry<String, Object>("parameters", parameters));
        return list;
    }

    /**
     * @param stackTrace the method invocation history
     * @return the first occurrence of {@link StackTraceElement} with a line number, for example:
     * <pre>" com.apache.a4javadoc.javaagent.agent.test.AgentTest.test$original$k72rSx58(AgentTest.java:37) "</pre>
     */
    private String findCaller(StackTraceElement[] stackTrace) {
        for(int i = 3; i < stackTrace.length; i++) {
            if (stackTrace[i].getLineNumber() > -1) {
                return " " + stackTrace[i].toString() + " ";
            }
        }
        return null;
    }

    /**
     * @param instrumentalizedMethod contains parameters type
     * @param instrumentalizedParameters contains parameters value
     * @return for example:
     * <pre>
     * [java.lang.String=Parameter, int=1, java.lang.StringBuilder=StringBuilder parameter.]
     * </pre>
     */
    private List<Entry<String, Object>> generateParameters(Method instrumentalizedMethod, Object[] instrumentalizedParameters) {
        List<Entry<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < instrumentalizedParameters.length; i++) {
            String type = instrumentalizedMethod.getParameterTypes()[i].getName();
            Object value = instrumentalizedParameters[i];
            AbstractMap.SimpleEntry<String, Object> entry = new AbstractMap.SimpleEntry<>(type, value);
            list.add(entry);
        }
        return list;
    }

    /**
     * @param instrumentalizedObject contains class type
     * @param instrumentalizedMethod contains parameters type
     * @return for example:
     * <pre>com.apache.a4javadoc.javaagent.agent.test.AgentTest(java.lang.String, int, java.lang.StringBuilder)</pre>
     */
    private String generateMethodName(Object instrumentalizedObject, Method instrumentalizedMethod) {
        StringBuilder result = new StringBuilder();
        result.append(instrumentalizedObject.getClass().getName())
            .append(".")
            .append(instrumentalizedMethod.getName())
            .append("(");
        for (int i = 0; i < instrumentalizedMethod.getParameterTypes().length; i++) {
            result.append(instrumentalizedMethod.getParameterTypes()[i].getName());
            if (i < instrumentalizedMethod.getParameterTypes().length - 1) {
                result.append(", ");
            }
        }
        result.append(")");
        return result.toString();
    }
    
    // TODO vyrobit testy a pak refakturovat projekty
//    a4javadoc
//    a4javadoc-common
//    a4javadoc-javaagent
//        a4javadoc-javaagent-core
//        a4javadoc-javaagent-api
//        a4javadoc-javaagent-plugins
//            a4javadoc-javaagent-plugins-recorder-to-a-log
//            a4javadoc-javaagent-plugins-recorder-to-a-file
//            ...
//    a4javadoc-collector
//        a4javadoc-collector-core
//        a4javadoc-collector-api
//        a4javadoc-collector-plugins
//            a4javadoc-collector-plugins-mongosaver
//            ...
//    a4javadoc-webapp

}
