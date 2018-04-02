package com.apache.a4javadoc.javaagent.recorder;

import java.io.StringWriter;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;
import com.apache.a4javadoc.javaagent.mapper.ObjectMapperA4j;
import com.apache.a4javadoc.javaagent.spring.SpringInitializer;

/** 
 * @author Kyrylo Semenko
 */
@Extension
public class Recorder implements MethodStateRecorder {
    
    private static final Logger logger = LoggerFactory.getLogger(Recorder.class);
    
    /** TODO Kyrylo Semenko */
    public Recorder() {
        logger.info("Recorder constructed");
    }
    
    /** 
     * @see com.apache.a4javadoc.javaagent.api.MethodStateRecorder#recordBefore(java.util.concurrent.Callable, java.lang.Object[])
     */
    @Override
    public void recordBefore(Callable<?> zuper, Object... args) {
        StringWriter stringWriterBefore = new StringWriter();
        SpringInitializer.getApplicationContext().getBean(ObjectMapperA4j.class).writeValue(stringWriterBefore, args);
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
        SpringInitializer.getApplicationContext().getBean(ObjectMapperA4j.class).writeValue(stringWriterAfter, args);
        logger.info("args after: {}", stringWriterAfter.toString());
//        logger.info("{}, id: {} args after: {}", stackTraceElements[2], counter, stringWriterAfter.toString());
        
        StringWriter stringWriterReturned = new StringWriter();
//        context.getBean(ObjectMapperA4j.class).writeValue(stringWriterReturned, result);
//        logger.info("{}, id: {} returned: {}", stackTraceElements[2], counter, stringWriterReturned.toString());
//        return result;

    }

    /**  */
    private String print(Object[] args) {
        StringBuilder result = new StringBuilder();
        for (Object object : args) {
            result.append(", " + object);
        }
        return result.toString();
    }

}
