package com.apache.a4javadoc.javaagent.api;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * Contains objects representing the state of the method before or after its invocation or in case of an exception.
 * 
 * @author Kyrylo Semenko
 */
public class MethodStateContainer {
    
    /** Identifier of the method invocation. It is the same before and after the method invocation as well as in case of exception. */
    private Long methodInvocationId;
    
    /** Date of the {@link MethodStateContainer} instantiation */
    private Date date;
    
    /** The current {@link StackTraceElement}s for tracking the method history */
    private StackTraceElement[] stackTrace;
    
    /** The invoked method */
    private Method instrumentalizedMethod;
    
    /** The instance of the method holder */
    private Object instrumentalizedObject;
    
    /** The invoked method parameters */
    Object[] instrumentalizedParameters;

    /**
     * @param methodInvocationId see {@link #methodInvocationId}
     * @param date see {@link #date}
     * @param stackTrace see {@link #stackTrace}
     * @param instrumentalizedMethod see {@link #instrumentalizedMethod}
     * @param instrumentalizedObject see {@link #instrumentalizedObject}
     * @param instrumentalizedParameters see {@link #instrumentalizedParameters}
     */
    public MethodStateContainer(Long methodInvocationId, 
            Date date,
            StackTraceElement[] stackTrace, 
            Method instrumentalizedMethod, 
            Object instrumentalizedObject, 
            Object[] instrumentalizedParameters) {
        this.methodInvocationId = methodInvocationId;
        this.date = date;
        this.stackTrace = stackTrace;
        this.instrumentalizedMethod = instrumentalizedMethod;
        this.instrumentalizedObject = instrumentalizedObject;
        this.instrumentalizedParameters = instrumentalizedParameters;
    }

    /** @return The {@link MethodStateContainer#methodInvocationId} field */
    public Long getMethodInvocationId() {
        return methodInvocationId;
    }

    /** See the {@link MethodStateContainer#methodInvocationId} field */
    public void setMethodInvocationId(Long methodInvocationId) {
        this.methodInvocationId = methodInvocationId;
    }

    /** @return The {@link MethodStateContainer#date} field */
    public Date getDate() {
        return date;
    }

    /** See the {@link MethodStateContainer#date} field */
    public void setDate(Date date) {
        this.date = date;
    }

    /** @return The {@link MethodStateContainer#stackTrace} field */
    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    /** See the {@link MethodStateContainer#stackTrace} field */
    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    /** @return The {@link MethodStateContainer#instrumentalizedMethod} field */
    public Method getInstrumentalizedMethod() {
        return instrumentalizedMethod;
    }

    /** See the {@link MethodStateContainer#instrumentalizedMethod} field */
    public void setInstrumentalizedMethod(Method instrumentalizedMethod) {
        this.instrumentalizedMethod = instrumentalizedMethod;
    }

    /** @return The {@link MethodStateContainer#instrumentalizedObject} field */
    public Object getInstrumentalizedObject() {
        return instrumentalizedObject;
    }

    /** See the {@link MethodStateContainer#instrumentalizedObject} field */
    public void setInstrumentalizedObject(Object instrumentalizedObject) {
        this.instrumentalizedObject = instrumentalizedObject;
    }

    /** @return The {@link MethodStateContainer#instrumentalizedParameters} field */
    public Object[] getInstrumentalizedParameters() {
        return instrumentalizedParameters;
    }

    /** See the {@link MethodStateContainer#instrumentalizedParameters} field */
    public void setInstrumentalizedParameters(Object[] instrumentalizedParameters) {
        this.instrumentalizedParameters = instrumentalizedParameters;
    }

}
