package com.apache.a4javadoc.javaagent.api;

import java.util.Date;

import org.pf4j.ExtensionPoint;

/** 
 * The interface of javaagent module for plugins.
 * It contains three methods:<br>
 * 1. {@link #recordBefore(MethodStateContainer)}<br>
 * 2. {@link #recordThrowable(MethodStateContainer, Exception)}<br>
 * 3. {@link #recordAfter(MethodStateContainer, Object)}<br>
 * The interface extends {@link ExtensionPoint}.
 * @author Kyrylo Semenko
 */
public interface MethodStateRecorder extends ExtensionPoint {
    
    /**
     * Save a method state before its calling
     * @param methodStateContainer see {@link MethodStateContainer#MethodStateContainer(Long, Date, StackTraceElement[], java.lang.reflect.Method, Object, Object[])}
     */
    public void recordBefore(MethodStateContainer methodStateContainer);
    
    /**
     * Save a method state after its calling
     * @param methodStateContainer see {@link MethodStateContainer#MethodStateContainer(Long, Date, StackTraceElement[], java.lang.reflect.Method, Object, Object[])}
     * @param result an object returned from the instrumentalized method or null for the void method.
     */
    public void recordAfter(MethodStateContainer methodStateContainer, Object result);
    
    /**
     * Save a method state in case of exception occurred during its invocation
     * @param methodStateContainer see {@link MethodStateContainer#MethodStateContainer(Long, Date, StackTraceElement[], java.lang.reflect.Method, Object, Object[])}
     * @param exeption the exception from the method
     */
    public void recordThrowable(MethodStateContainer methodStateContainer, Exception exeption);

}
