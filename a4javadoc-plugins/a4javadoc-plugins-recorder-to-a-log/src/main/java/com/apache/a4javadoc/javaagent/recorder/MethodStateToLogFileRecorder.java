package com.apache.a4javadoc.javaagent.recorder;

import java.io.StringWriter;
import java.util.Date;
import java.util.concurrent.Callable;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;
import com.apache.a4javadoc.javaagent.mapper.ObjectMapperA4j;

/**
 * Implementation of {@link MethodStateRecorder} methods. Saves recorded data to an application logger.
 * @author Kyrylo Semenko
 */
@Extension
public class MethodStateToLogFileRecorder implements MethodStateRecorder {
    
    private static final Logger logger = LoggerFactory.getLogger(MethodStateToLogFileRecorder.class);
    
    /** Constructor */
    public MethodStateToLogFileRecorder() {
        logger.info("Recorder constructed");
    }
    
    @Override
    public void recordBefore(Long methodInvocationId, Date recordDate, StackTraceElement[] stackTraceElements, Callable<?> zuper, Object... args) {
// TODO Kyrylo Semenko        StateBeforeDto stateBeforeDto = new StateBeforeDto(mechodInvocationId, recordDate, stackTraceElements, zuper, args);
        
        StringWriter stringWriterBefore = new StringWriter()
        .append("State before: mechodInvocationId: ")
        .append(Long.toString(methodInvocationId))
        .append(", args: ");
        ObjectMapperA4j.getInstance().writeValue(stringWriterBefore, args);
        stringWriterBefore.append(", methodName: ")
        .append(zuper.toString());
        
        
        logger.info(stringWriterBefore.toString());
        
//        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//        logger.info("{}, id: {} args before: {}", stackTraceElements[2], counter, stringWriterBefore.toString());
        
//        for (Object object : args) {
//            System.out.println("object.getClass().getName(): " + object.getClass().getName());
//        }
        
//      StringWriter stringWriterBefore = new StringWriter();
//      context.getBean(ObjectMapperA4j.class).writeValue(stringWriterBefore, args);
//      StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//      logger.info("{}, id: {} args before: {}", stackTraceElements[2], counter, stringWriterBefore.toString());
//      
//      for (Object object : args) {
//          System.out.println("object.getClass().getName(): " + object.getClass().getName());
//      }
        

    }
    
    @Override
    public void recordThrowable(Long methodInvocationId, java.util.Date recordDate, java.lang.Object[] args) {
        StringWriter stringWriter = new StringWriter()
                .append("Exception: mechodInvocationId: ")
                .append(Long.toString(methodInvocationId))
                .append(", args: ");
                ObjectMapperA4j.getInstance().writeValue(stringWriter, args);
                
                
        logger.info(stringWriter.toString());
        
    }
    
    @Override
    public void recordAfter(Long methodInvocationId, java.util.Date recordDate, java.lang.Object result, java.lang.Object... args) {
        StringWriter stringWriter = new StringWriter()
                .append("State after: mechodInvocationId: ")
                .append(Long.toString(methodInvocationId))
                .append(", args: ");
        ObjectMapperA4j.getInstance().writeValue(stringWriter, args);
        
        
        logger.info(stringWriter.toString());
        
        
//        StringWriter stringWriterAfter = new StringWriter();
//        ObjectMapperA4j.getInstance().writeValue(stringWriterAfter, args);
//        logger.info("args after: {}", stringWriterAfter.toString());
//        logger.info("{}, id: {} args after: {}", stackTraceElements[2], counter, stringWriterAfter.toString());
        
//        StringWriter stringWriterReturned = new StringWriter();
//        context.getBean(ObjectMapperA4j.class).writeValue(stringWriterReturned, result);
//        logger.info("{}, id: {} returned: {}", stackTraceElements[2], counter, stringWriterReturned.toString());
//        return result;
        
        
//      StringWriter stringWriterAfter = new StringWriter();
//      context.getBean(ObjectMapperA4j.class).writeValue(stringWriterAfter, args);
//      logger.info("{}, id: {} args after: {}", stackTraceElements[2], counter, stringWriterAfter.toString());
//      
//      StringWriter stringWriterReturned = new StringWriter();
//      context.getBean(ObjectMapperA4j.class).writeValue(stringWriterReturned, result);
//      logger.info("{}, id: {} returned: {}", stackTraceElements[2], counter, stringWriterReturned.toString());
        // TODO Auto-generated method stub
        
    }

}
