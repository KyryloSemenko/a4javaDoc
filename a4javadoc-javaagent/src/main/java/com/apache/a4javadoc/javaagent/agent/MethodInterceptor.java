package com.apache.a4javadoc.javaagent.agent;

import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

/**
 * The class is instantiated by Spring container and 
 * @author Kyrylo Semenko
 */
public class MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(MethodInterceptor.class);
    
    private AtomicLong callCounter;
    
    private List<MethodStateRecorder> recorders;
    
    /**  */
    public MethodInterceptor() {
        callCounter = new AtomicLong(0L);
        // create the plugin manager
        final PluginManager pluginManager = new DefaultPluginManager();

        // load the plugins
        pluginManager.loadPlugins();

        // start (active/resolved) the plugins
        pluginManager.startPlugins();

        // retrieves the extensions for Greeting extension point
        recorders = pluginManager.getExtensions(MethodStateRecorder.class);
        // TODO Kyrylo Semenko
        recorders.remove(1);
        logger.info(String.format("Found %d extensions for extension point '%s'", recorders.size(), MethodStateRecorder.class.getName()));
    }
    
    // TODO Kyrylo Semenko
    @RuntimeType
    public Object intercept(@SuperCall Callable<?> zuper, @AllArguments Object... args) throws Exception {
        Long counter = callCounter.incrementAndGet();
        
        for (MethodStateRecorder methodStateRecorder : recorders) {
            methodStateRecorder.recordBefore(zuper, args);
        }
        
//        StringWriter stringWriterBefore = new StringWriter();
//        context.getBean(ObjectMapperA4j.class).writeValue(stringWriterBefore, args);
//        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//        logger.info("{}, id: {} args before: {}", stackTraceElements[2], counter, stringWriterBefore.toString());
//        
//        for (Object object : args) {
//            System.out.println("object.getClass().getName(): " + object.getClass().getName());
//        }
        
        Object result = zuper.call();

        for (MethodStateRecorder methodStateRecorder : recorders) {
            methodStateRecorder.recordAfter(zuper, args);
        }

//        StringWriter stringWriterAfter = new StringWriter();
//        context.getBean(ObjectMapperA4j.class).writeValue(stringWriterAfter, args);
//        logger.info("{}, id: {} args after: {}", stackTraceElements[2], counter, stringWriterAfter.toString());
//        
//        StringWriter stringWriterReturned = new StringWriter();
//        context.getBean(ObjectMapperA4j.class).writeValue(stringWriterReturned, result);
//        logger.info("{}, id: {} returned: {}", stackTraceElements[2], counter, stringWriterReturned.toString());
        return result;
    }
}
