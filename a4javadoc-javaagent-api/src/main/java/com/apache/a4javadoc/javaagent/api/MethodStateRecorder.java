package com.apache.a4javadoc.javaagent.api;

import java.util.Date;

import org.pf4j.ExtensionPoint;

/** 
 * The interface of javaagent module for plugins.
 * It contains these methods:<br>
 * 1. {@link #recordBefore(StateBeforeInvocation)}<br>
 * 2. {@link #recordAfter(StateAfterInvocation)}<br>
 * The interface extends {@link ExtensionPoint}.
 * @author Kyrylo Semenko
 */
public interface MethodStateRecorder extends ExtensionPoint {
    
    /**
     * Save a method state before its calling
     * @param stateBeforeInvocation see {@link StateBeforeInvocation#StateBeforeInvocation(long, Date, StackTraceElement[], String, String, String, String, String, String, Object[])}
     */
    public void recordBefore(StateBeforeInvocation stateBeforeInvocation);
    
    /**
     * Save a method state after its calling
     * @param stateAfterInvocation see {@link StateAfterInvocation#StateAfterInvocation(long, Date, String, Object, Throwable, Object[])}
     */
    public void recordAfter(StateAfterInvocation stateAfterInvocation);
    
}
