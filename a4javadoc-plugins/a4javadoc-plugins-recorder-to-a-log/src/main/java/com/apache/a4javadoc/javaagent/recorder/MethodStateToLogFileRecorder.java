package com.apache.a4javadoc.javaagent.recorder;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;
import com.apache.a4javadoc.javaagent.api.StateAfterInvocation;
import com.apache.a4javadoc.javaagent.api.StateBeforeInvocation;
import com.apache.a4javadoc.javaagent.mapper.ObjectMapperA4j;

/**
 * Implementation of {@link MethodStateRecorder} methods. Saves recorded data to an application logger.
 * @author Kyrylo Semenko
 */
@Extension
public class MethodStateToLogFileRecorder implements MethodStateRecorder {
    
    private static final int MAX_NUMBER_OF_STACK_TRACE_ELEMENTS = 25;
    static final String REMOVED_BECAUSE_THE_OBJECT_CONTAINED_A_CIRCULAR_DEPENDENCY = "Removed because the object contained a circular dependency.";
    private static final int MAX_DEPTH_OF_DIVING_INTO_OBJECT = 7;
    private static final Logger logger = LoggerFactory.getLogger(MethodStateToLogFileRecorder.class);
    
    /** Constructor */
    public MethodStateToLogFileRecorder() {
        logger.info("Recorder constructed");
    }
    
    @Override
    public void recordBefore(StateBeforeInvocation stateBeforeInvocation) {
        truncateStackTrace(stateBeforeInvocation);
        removeCircularObjects(stateBeforeInvocation.getAllArguments());
        logger.info("Method starts: {}", stateBeforeInvocation.getMethodComplexName());
        StringWriter stringWriter = new StringWriter().append("State before: ");
        ObjectMapperA4j.getInstance().writeValue(stringWriter, stateBeforeInvocation);
        if (logger.isInfoEnabled()) {
            logger.info(stringWriter.toString());
        }
    }

    @Override
    public void recordAfter(StateAfterInvocation stateAfterInvocation) {
        logger.info("Method ended: {}", stateAfterInvocation.getMethodComplexName());
        removeCircularObjects(stateAfterInvocation.getAllArguments());
        StringWriter stringWriter = new StringWriter().append("State   after: ");
        ObjectMapperA4j.getInstance().writeValue(stringWriter, stateAfterInvocation);
        if (logger.isInfoEnabled()) {
            logger.info(stringWriter.toString());
        }
    }
    
    /** Find out the first closest caller of the method */
    private void truncateStackTrace(StateBeforeInvocation stateBeforeInvocation) {
        StackTraceElement[] stackTraceElements = stateBeforeInvocation.getStackTrace();
        if (stackTraceElements.length > MAX_NUMBER_OF_STACK_TRACE_ELEMENTS) {
            StackTraceElement[] copy = Arrays.copyOfRange(stackTraceElements, 0, MAX_NUMBER_OF_STACK_TRACE_ELEMENTS);
            StackTraceElement stackTraceElement = new StackTraceElement("", "Other elements has been removed", "", 0);
            copy[MAX_NUMBER_OF_STACK_TRACE_ELEMENTS - 1] = stackTraceElement;
            stateBeforeInvocation.setStackTrace(copy);
        }
    }
    
    // TODO Kyrylo Semenko naucit se serializovat circular objects a smazat metodu
    void removeCircularObjects(Object[] allArguments) {
        for (int i = 0; i < allArguments.length; i++) {
            
            if (hasCircularDependency(allArguments[i])) {
                allArguments[i] = REMOVED_BECAUSE_THE_OBJECT_CONTAINED_A_CIRCULAR_DEPENDENCY;
            }
        }
    }

    // TODO Kyrylo Semenko naucit se serializovat circular objects a smazat metodu
    private boolean hasCircularDependency(Object object) {
        if (object == null) {
            return false;
        }
        Map<String, Object> map = new HashMap<>();
        map.putAll(getAllFields(object, object.getClass().getSimpleName(), 1));
        List<String> keys = new ArrayList<>(map.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = map.get(key);
            if (value != null) {
                for (int k = i + 1; k < keys.size(); k++) {
                    String keyInner = keys.get(k);
                    Object valueInner = map.get(keyInner);
                    if (value.equals(valueInner)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // TODO Kyrylo Semenko naucit se serializovat circular objects a smazat metodu
    private Map<String, Object> getAllFields(Object object, String prefix, int depth) {
        try {
            Map<String, Object> map = new HashMap<>();
            if (object == null) {
                return map;
            }
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.getType().isPrimitive()) {
                    String newPrefix = prefix + "." + field.getName();
                    field.setAccessible(true);
                    Object newObject = field.get(object);
                    map.put(newPrefix, newObject);
                    if (depth < MAX_DEPTH_OF_DIVING_INTO_OBJECT) {
                        map.putAll(getAllFields(newObject, newPrefix, depth + 1));
                    }
                }
            }
            return map;
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * @param instrumentedMethod contains a type
     * @param result contains a value
     * @return for example:
     * <pre>boolean:true</pre>
     * <pre>void:null</pre>
     */
    private Entry<String, String> generateResult(Object instrumentedMethod, Object result) {
//        return new AbstractMap.SimpleEntry<>(instrumentedMethod.getReturnType().getName(), String.valueOf(result));
        return new AbstractMap.SimpleEntry<String, String>(instrumentedMethod.toString(), String.valueOf(result));
    }

//    /**
//     * Prepare data for a JSON string
//     * @param methodStateContainer the data source
//     */
//    private List<Entry<String, Object>> generateStateMap(MethodStateContainer methodStateContainer) {
//        List<Entry<String, Object>> list = new ArrayList<Entry<String, Object>>();
//        
//        AbstractMap.SimpleEntry<String, Object> idEntry = new AbstractMap.SimpleEntry<String, Object>("methodInvocationId", (Object) methodStateContainer.getMethodInvocationId());
//        list.add(idEntry);
//
//        String caller = findCaller(methodStateContainer.getStackTrace());
//        list.add(new AbstractMap.SimpleEntry<String, Object>("caller", caller));
//        
//        String nameEntry = generateMethodName(methodStateContainer.getInstrumentedObject(), methodStateContainer.getInstrumentedMethod());
//        list.add(new AbstractMap.SimpleEntry<String, Object>("name", nameEntry));
//        
//        List<Entry<String, Object>> parameters = generateParameters(methodStateContainer.getInstrumentedMethod(), methodStateContainer.getInstrumentedParameters());
//        list.add(new AbstractMap.SimpleEntry<String, Object>("parameters", parameters));
//        return list;
//    }

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
     * @param instrumentedMethod contains parameters type
     * @param instrumentedParameters contains parameters value
     * @return for example:
     * <pre>
     * [java.lang.String=Parameter, int=1, java.lang.StringBuilder=StringBuilder parameter.]
     * </pre>
     */
    private List<Entry<String, Object>> generateParameters(Method instrumentedMethod, Object[] instrumentedParameters) {
        List<Entry<String, Object>> list = new ArrayList<Entry<String, Object>>();
        for (int i = 0; i < instrumentedParameters.length; i++) {
            String type = instrumentedMethod.getParameterTypes()[i].getName();
            Object value = instrumentedParameters[i];
            AbstractMap.SimpleEntry<String, Object> entry = new AbstractMap.SimpleEntry<String, Object>(type, value);
            list.add(entry);
        }
        return list;
    }

    /**
     * @param instrumentedObject contains class type
     * @param instrumentedMethod contains parameters type
     * @return for example:
     * <pre>com.apache.a4javadoc.javaagent.agent.test.AgentTest(java.lang.String, int, java.lang.StringBuilder)</pre>
     */
    private String generateMethodName(Object instrumentedObject, Method instrumentedMethod) {
        StringBuilder result = new StringBuilder();
        result.append(instrumentedObject.getClass().getName())
            .append(".")
            .append(instrumentedMethod.getName())
            .append("(");
        for (int i = 0; i < instrumentedMethod.getParameterTypes().length; i++) {
            result.append(instrumentedMethod.getParameterTypes()[i].getName());
            if (i < instrumentedMethod.getParameterTypes().length - 1) {
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
