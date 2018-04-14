package com.apache.a4javadoc.javaagent.recorder;

import java.io.StringWriter;
import java.util.concurrent.Callable;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;
import com.apache.a4javadoc.javaagent.mapper.ObjectMapperA4j;

/** 
 * @author Kyrylo Semenko
 */
@Extension
public class Recorder implements MethodStateRecorder {
    
    private static final Logger logger = LoggerFactory.getLogger(Recorder.class);
    
    /** Constructor */
    public Recorder() {
        logger.info("Recorder constructed");
    }
    
    /** 
     * @see com.apache.a4javadoc.javaagent.api.MethodStateRecorder#recordBefore(java.util.concurrent.Callable, java.lang.Object[])
     */
    @Override
    public void recordBefore(Callable<?> zuper, Object... args) {
        StringWriter stringWriterBefore = new StringWriter();
        ObjectMapperA4j.getInstance().writeValue(stringWriterBefore, args);
        logger.info("args before: {}", stringWriterBefore.toString());
//        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//        logger.info("{}, id: {} args before: {}", stackTraceElements[2], counter, stringWriterBefore.toString());
        
//        for (Object object : args) {
//            System.out.println("object.getClass().getName(): " + object.getClass().getName());
//        }
        

    }

    /** 
     * @see com.apache.a4javadoc.javaagent.api.MethodStateRecorder#recordAfter(java.util.concurrent.Callable, java.lang.Object[])
     */
    @Override
    public void recordAfter(Callable<?> zuper, Object... args) {
        StringWriter stringWriterAfter = new StringWriter();
        ObjectMapperA4j.getInstance().writeValue(stringWriterAfter, args);
        logger.info("args after: {}", stringWriterAfter.toString());
//        logger.info("{}, id: {} args after: {}", stackTraceElements[2], counter, stringWriterAfter.toString());
        
        StringWriter stringWriterReturned = new StringWriter();
//        context.getBean(ObjectMapperA4j.class).writeValue(stringWriterReturned, result);
//        logger.info("{}, id: {} returned: {}", stackTraceElements[2], counter, stringWriterReturned.toString());
//        return result;

    }

}
