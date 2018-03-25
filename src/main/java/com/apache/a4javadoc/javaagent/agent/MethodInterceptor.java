package com.apache.a4javadoc.javaagent.agent;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.apache.a4javadoc.javaagent.mapper.ObjectMapperA4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

/**
 * The class is instantiated by Spring container and 
 * @author Kyrylo Semenko
 */
@Service
public class MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(MethodInterceptor.class);
    
    @Autowired
    private ApplicationContext context;
    
    // TODO Kyrylo Semenko
    @RuntimeType
    public Object intercept(@SuperCall Callable<?> zuper, @AllArguments Object... args) throws Exception {
        StringWriter stringWriterBefore = new StringWriter();
        context.getBean(ObjectMapperA4j.class).writeValue(stringWriterBefore, args);
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        logger.info("{} args before: {}", stackTraceElements[2], stringWriterBefore.toString());
        
        Object result = zuper.call();

        StringWriter stringWriterAfter = new StringWriter();
        context.getBean(ObjectMapperA4j.class).writeValue(stringWriterAfter, args);
        logger.info("{} args after: {}", stackTraceElements[2], stringWriterAfter.toString());
        
        StringWriter stringWriterReturned = new StringWriter();
        context.getBean(ObjectMapperA4j.class).writeValue(stringWriterReturned, args);
        logger.info("{} returned: {}", stackTraceElements[2], stringWriterReturned.toString());
        return result;
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
