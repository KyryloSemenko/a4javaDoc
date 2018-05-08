package com.apache.a4javadoc.javaagent.api;

import java.util.Date;

/**
 * Contains objects representing the state of a method before its invocation.
 * @author Kyrylo Semenko
 */
public class StateBeforeInvocation {
    
    /** Identifier of the method invocation. It is the same before and after the method invocation. */
    private Long methodInvocationId;
    
    /** Date of the instrumented method invocation */
    private Date date;
    
    /** The invoked method name, <i>clinit</i> for a static initialization block, <i>init</i> for a constructors */
    private String methodName;
    
    /** The invoked method type, for example <pre>com.apache.a4javadoc.javaagent.agent.test.AgentTest</pre> */
    private String declaringType;
    
    /** The invoked method descriptor, for example <pre>(Ljava/lang/String;ILjava/lang/StringBuilder;)Z</pre> */
    private String methodDescriptor;
    
    /** The invoked method signature, for example <pre>(java.lang.String,int,java.lang.StringBuilder)</pre> */
    private String methodSignature;
    
    /** The invoked method return type, for example <pre>boolean</pre> */
    private String returnType;
    
    /** Full name of the invoked method, for example <pre>private boolean com.apache.a4javadoc.javaagent.agent.test.AgentTest.method(java.lang.String,int,java.lang.StringBuilder)</pre> */
    private String methodComplexName;
    
    /** The invoked method parameters */
    private Object[] allArguments;
    
    /** The current {@link StackTraceElement}s for tracking the method history */
    private StackTraceElement[] stackTrace;

    /**
     * @param methodInvocationId see the {@link #methodInvocationId} field
     * @param date see the {@link #date} field
     * @param stackTrace see the {@link #stackTrace} field
     * @param methodName see the {@link #methodName} field
     * @param declaringType see the {@link #declaringType} field
     * @param methodDescriptor see the {@link #methodDescriptor} field
     * @param methodSignature see the {@link #methodSignature} field
     * @param returnType see the {@link #returnType} field
     * @param methodComplexName see the {@link #methodComplexName} field
     * @param allArguments see the {@link #allArguments} field
     */
    public StateBeforeInvocation( // NOSONAR
            long methodInvocationId,
            Date date,
            StackTraceElement[] stackTrace,
            String methodName,
            String declaringType,
            String methodDescriptor,
            String methodSignature,
            String returnType,
            String methodComplexName,
            Object[] allArguments) {
        this.methodInvocationId = methodInvocationId;
        this.date = date;
        this.stackTrace = stackTrace;
        this.methodName = methodName;
        this.declaringType = declaringType;
        this.methodDescriptor = methodDescriptor;
        this.methodSignature = methodSignature;
        this.returnType = returnType;
        this.methodComplexName = methodComplexName;
        this.allArguments = allArguments;
    }

    /** @return The {@link StateBeforeInvocation#methodInvocationId} field */
    public Long getMethodInvocationId() {
        return methodInvocationId;
    }

    /** @param methodInvocationId see the {@link StateBeforeInvocation#methodInvocationId} field */
    public void setMethodInvocationId(Long methodInvocationId) {
        this.methodInvocationId = methodInvocationId;
    }

    /** @return The {@link StateBeforeInvocation#date} field */
    public Date getDate() {
        return date;
    }

    /** @param date see the {@link StateBeforeInvocation#date} field */
    public void setDate(Date date) {
        this.date = date;
    }

    /** @return The {@link StateBeforeInvocation#methodName} field */
    public String getMethodName() {
        return methodName;
    }

    /** @param methodName see the {@link StateBeforeInvocation#methodName} field */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /** @return The {@link StateBeforeInvocation#declaringType} field */
    public String getDeclaringType() {
        return declaringType;
    }

    /** @param declaringType see the {@link StateBeforeInvocation#declaringType} field */
    public void setDeclaringType(String declaringType) {
        this.declaringType = declaringType;
    }

    /** @return The {@link StateBeforeInvocation#methodDescriptor} field */
    public String getMethodDescriptor() {
        return methodDescriptor;
    }

    /** @param methodDescriptor see the {@link StateBeforeInvocation#methodDescriptor} field */
    public void setMethodDescriptor(String methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    /** @return The {@link StateBeforeInvocation#methodSignature} field */
    public String getMethodSignature() {
        return methodSignature;
    }

    /** @param methodSignature see the {@link StateBeforeInvocation#methodSignature} field */
    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    /** @return The {@link StateBeforeInvocation#returnType} field */
    public String getReturnType() {
        return returnType;
    }

    /** @param returnType see the {@link StateBeforeInvocation#returnType} field */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    /** @return The {@link StateBeforeInvocation#methodComplexName} field */
    public String getMethodComplexName() {
        return methodComplexName;
    }

    /** @param methodComplexName see the {@link StateBeforeInvocation#methodComplexName} field */
    public void setMethodComplexName(String methodComplexName) {
        this.methodComplexName = methodComplexName;
    }

    /** @return The {@link StateBeforeInvocation#allArguments} field */
    public Object[] getAllArguments() {
        return allArguments;
    }

    /** @param allArguments see the {@link StateBeforeInvocation#allArguments} field */
    public void setAllArguments(Object[] allArguments) {
        this.allArguments = allArguments;
    }
    
    /** @return The {@link StateBeforeInvocation#stackTrace} field */
    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }
    
    /** @param stackTrace see the {@link StateBeforeInvocation#stackTrace} field */
    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }

}
