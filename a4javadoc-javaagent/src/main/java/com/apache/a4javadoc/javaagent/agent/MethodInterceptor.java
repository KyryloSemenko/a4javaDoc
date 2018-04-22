package com.apache.a4javadoc.javaagent.agent;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.apache.a4javadoc.javaagent.api.MethodStateContainer;
import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;
import com.apache.a4javadoc.plugin.AgentPluginManager;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * The class is instantiated by the {@link Agent}. It contains a state object {@link #methodInvocationCounter}. It is a singleton.<br>
 * The single instance of the class contains the {@link #intercept(Callable, Method, Object, Object[])} method where an intercepted method will be instrumentalized.
 * @author Kyrylo Semenko
 */
public class MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(MethodInterceptor.class);
    
    /** Each method invocation has its own identifier. It starts from 1 when an instrumentalized application launched. */
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
     * Obtains a proxy method of an examined class.<br>
     * The proxy method will be instrumentalized by an additional behavior before and after its invocation.<br>
     * In case of exception occurred in the method the additional behavior will be inserted too.<br>
     * The additional behavior (before, after, try - catch) is inserted by plugins, see a {@link AgentPluginManager#getMethodStateRecorders()}.
     * @param zuper a proxy method of an examined method
     * @param instrumentalizedMethod the examined method
     * @param instrumentalizedObject the instance of an object with the examined method
     * @param instrumentalizedParameters parameters of the examined method
     * @return returned value of the instrumentalized examined proxy method
     * @throws Exception re-throws a zuper method exception
     */
//    @RuntimeType
//    public Object intercept(@SuperCall Callable<?> zuper,
//            @Origin Method instrumentalizedMethod,
//            @This Object instrumentalizedObject,
//            @AllArguments Object[] instrumentalizedParameters) throws Exception {
//        Long methodInvocationId = methodInvocationCounter.incrementAndGet();
//        
//        for (MethodStateRecorder methodStateRecorder : AgentPluginManager.getInstance().getMethodStateRecorders()) {
//            MethodStateContainer methodStateContainer = new MethodStateContainer(methodInvocationId, new Date(), Thread.currentThread().getStackTrace(), instrumentalizedMethod, instrumentalizedObject, instrumentalizedParameters);
//            methodStateRecorder.recordBefore(methodStateContainer);
//        }
//        
//        Object result = null;
//        try {
//            result = zuper.call();
//        } catch (Exception e) {
//            for (MethodStateRecorder methodStateRecorder : AgentPluginManager.getInstance().getMethodStateRecorders()) {
//                MethodStateContainer methodStateContainer = new MethodStateContainer(methodInvocationId, new Date(), Thread.currentThread().getStackTrace(), instrumentalizedMethod, instrumentalizedObject, instrumentalizedParameters);
//                methodStateRecorder.recordThrowable(methodStateContainer, e);
//            }
//            throw e;
//        }
//
//        for (MethodStateRecorder methodStateRecorder : AgentPluginManager.getInstance().getMethodStateRecorders()) {
//            MethodStateContainer methodStateContainer = new MethodStateContainer(methodInvocationId, new Date(), Thread.currentThread().getStackTrace(), instrumentalizedMethod, instrumentalizedObject, instrumentalizedParameters);
//            methodStateRecorder.recordAfter(methodStateContainer, result);
//        }
//        return result;
//    }


    @RuntimeType
    public static Object interceptStatic(@AllArguments Object[] instrumentalizedParameters) throws Exception {
        
        logger.info("Static interceptor");
        return null;
    }
}
