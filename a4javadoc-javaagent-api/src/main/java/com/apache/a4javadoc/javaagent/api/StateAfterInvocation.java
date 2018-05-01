package com.apache.a4javadoc.javaagent.api;

import java.util.Date;

/**
 * Contains objects representing the state of a method after its invocation
 * @author Kyrylo Semenko
 */
public class StateAfterInvocation {
    /** Identifier of the method invocation. It is the same before and after the method invocation. */
    private Long methodInvocationId;
    
    /** Date of the instrumented method finish */
    private Date date;

    /** Full name of the invoked method, for example <pre>private boolean com.apache.a4javadoc.javaagent.agent.test.AgentTest.method(java.lang.String,int,java.lang.StringBuilder)</pre> */
    private String methodComplexName;
    
    /** The value returned from the invoked method */
    private Object returnValue;
    
    /** The exception occurred in the invoked method */
    private Throwable throwable;
    
    /** The invoked method parameters */
    private Object[] allArguments;

    /** 
     * @param methodInvocationId see the {@link #methodInvocationId} field
     * @param date see the {@link #date} field
     * @param methodComplexName see the {@link #methodComplexName} field
     * @param returnValue see the {@link #returnValue} field
     * @param throwable see the {@link #throwable} field
     * @param allArguments see the {@link #allArguments} field
     */
    public StateAfterInvocation(
            long methodInvocationId,
            Date date,
            String methodComplexName,
            Object returnValue,
            Throwable throwable,
            Object[] allArguments) {
        this.methodInvocationId = methodInvocationId;
        this.date = date;
        this.methodComplexName = methodComplexName;
        this.returnValue = returnValue;
        this.throwable = throwable;
        this.allArguments = allArguments;
    }

    /** @return The {@link StateAfterInvocation#methodInvocationId} field */
    public Long getMethodInvocationId() {
        return methodInvocationId;
    }

    /** @param methodInvocationId see the {@link StateAfterInvocation#methodInvocationId} field */
    public void setMethodInvocationId(Long methodInvocationId) {
        this.methodInvocationId = methodInvocationId;
    }

    /** @return The {@link StateAfterInvocation#date} field */
    public Date getDate() {
        return date;
    }

    /** @param date see the {@link StateAfterInvocation#date} field */
    public void setDate(Date date) {
        this.date = date;
    }

    /** @return The {@link StateAfterInvocation#methodComplexName} field */
    public String getMethodComplexName() {
        return methodComplexName;
    }

    /** @param methodComplexName see the {@link StateAfterInvocation#methodComplexName} field */
    public void setMethodComplexName(String methodComplexName) {
        this.methodComplexName = methodComplexName;
    }

    /** @return The {@link StateAfterInvocation#returnValue} field */
    public Object getReturnValue() {
        return returnValue;
    }

    /** @param returnValue see the {@link StateAfterInvocation#returnValue} field */
    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    /** @return The {@link StateAfterInvocation#throwable} field */
    public Throwable getThrowable() {
        return throwable;
    }

    /** @param throwable see the {@link StateAfterInvocation#throwable} field */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /** @return The {@link StateAfterInvocation#allArguments} field */
    public Object[] getAllArguments() {
        return allArguments;
    }

    /** @param allArguments see the {@link StateAfterInvocation#allArguments} field */
    public void setAllArguments(Object[] allArguments) {
        this.allArguments = allArguments;
    }

}
