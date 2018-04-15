package com.apache.a4javadoc.javaagent.api;

import java.util.Date;
import java.util.concurrent.Callable;
import org.pf4j.ExtensionPoint;

/** 
 * The interface of javaagent module for plugins.
 * It contains three methods:<br>
 * 1. {@link #recordBefore(Long, Date, StackTraceElement[], Callable, Object...)}<br>
 * 2. {@link #recordThrowable(Long, Date, Object[])}<br>
 * 3. {@link #recordAfter(Long, Date, Object, Object...)}<br>
 * The interface extends {@link ExtensionPoint}.
 * @author Kyrylo Semenko
 */
public interface MethodStateRecorder extends ExtensionPoint {
    
    /**
     * Save a method state before its calling
     * @param methodInvocationId an identifier of the method invocation, the same as in {@link #recordThrowable(Long, Date, Object[])} and {@link #recordAfter(Long, Date, Object, Object...)} methods
     * @param stackTraceElements an invocation sequence of the method
     * @param recordDate time stamp of the method invocation start
     * @param zuper an instrumented method
     * @param args parameters of the instrumented method
     */
    public void recordBefore(Long methodInvocationId, Date recordDate, StackTraceElement[] stackTraceElements, Callable<?> zuper, Object... args);
    
    /**
     * Save a method state in case of exception occurred during its invocation
     * @param methodInvocationId an identifier of the method invocation, the same as in {@link #recordBefore(Long, Date, StackTraceElement[], Callable, Object...)} and {@link #recordAfter(Long, Date, Object, Object...)} methods
     * @param recordDate time stamp of exception
     * @param args parameters of the instrumented method
     */
    public void recordThrowable(Long methodInvocationId, Date recordDate, Object[] args);
    
    /**
     * Save a method state after its calling
     * @param methodInvocationId an identifier of the method invocation, the same as in {@link #recordBefore(Long, Date, StackTraceElement[], Callable, Object...)} and {@link #recordThrowable(Long, Date, Object[])} methods
     * @param recordDate time stamp of the method invocation finish
     * @param result an object returned from the instrumented method or null for the void method.
     * @param args parameters of the instrumented method
     */
    public void recordAfter(Long methodInvocationId, Date recordDate, Object result, Object... args);

}
