package com.apache.a4javadoc.javaagent.agent;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;
import com.apache.a4javadoc.plugin.AgentPluginManager;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

/**
 * The class is instantiated by {@link Agent}. It contains a state object {@link #methodInvocationCounter}. It is a singleton.<br>
 * The single instance of the class contains the {@link #intercept(Callable, Object...)} method where an additional behavior appended before and after calling of an instrumented methods.
 * @author Kyrylo Semenko
 */
public class MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(MethodInterceptor.class);
    
    /** Each method invocation has its own identifier. It starts from 1 when an instrumented application launched. */
    private AtomicLong methodInvocationCounter;
    
    private static MethodInterceptor instance;
    
    /**
     * The static factory.
     * @return a singleton instance
     */
    public static MethodInterceptor getInstance() {
        if (instance == null) {
            instance = new MethodInterceptor();
        }
        return instance;
    }
    
    /** Initializes a {@link #methodInvocationCounter} to 0 */
    private MethodInterceptor() {
        if (methodInvocationCounter != null) {
            throw new AppRuntimeException("AtomicLong callCounter already set and has a value '" + methodInvocationCounter + "'. Creation of a second instance of the " + this.getClass().getName() + " class is not allowed.");
        }
        methodInvocationCounter = new AtomicLong(0L);
        logger.info("Construction of MethodInterceptor started. callCounter: '{}'", methodInvocationCounter.get());
        
        logger.info("Found {} extensions for extension point '{}'", AgentPluginManager.getInstance().getMethodStateRecorders().size(), MethodStateRecorder.class.getName());
    }
    
    /**
     * TODO Kyrylo Semenko
     */
    @RuntimeType
    public Object intercept(@SuperCall Callable<?> zuper, @AllArguments Object... args) throws Exception {
        Long methodInvocationId = methodInvocationCounter.incrementAndGet();
        
        for (MethodStateRecorder methodStateRecorder : AgentPluginManager.getInstance().getMethodStateRecorders()) {
            methodStateRecorder.recordBefore(methodInvocationId, new Date(), Thread.currentThread().getStackTrace(), zuper, args);
        }
        
        Object result = null;
        try {
            result = zuper.call();
        } catch (Throwable throwable) { //NOSONAR
            for (MethodStateRecorder methodStateRecorder : AgentPluginManager.getInstance().getMethodStateRecorders()) {
                methodStateRecorder.recordThrowable(methodInvocationId, new Date(), args);
            }
            throw throwable;
        }

        for (MethodStateRecorder methodStateRecorder : AgentPluginManager.getInstance().getMethodStateRecorders()) {
            methodStateRecorder.recordAfter(methodInvocationId, new Date(), result, args);
        }
        return result;
    }
}
