package com.apache.a4javadoc.javaagent.api;

import java.util.concurrent.Callable;
import org.pf4j.ExtensionPoint;

/** 
 * The interface of javaagent module for plugins.
 * @author Kyrylo Semenko
 */
public interface MethodStateRecorder extends ExtensionPoint {
    
    /**
     * Save a method state before its calling
     * @param zuper an instrumented method
     * @param args parameters of the instrumented method
     */
    public void recordBefore(Callable<?> zuper, Object... args);
    
    /**
     * Save a method state after its calling
     * @param zuper an instrumented method
     * @param args parameters of the instrumented method
     */
    public void recordAfter(Callable<?> zuper, Object... args);

}
