package com.apache.a4javadoc.javaagent.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** 
 * @author Kyrylo Semenko
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan("com.apache.a4javadoc.javaagent")
public class SpringInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringInitializer.class);
    
    private static final ApplicationContext applicationContext = createSpringContext();
    
    /**
     * An empty main method for development purposes only.
     */
    public static ApplicationContext createSpringContext() {
        ApplicationContext applicationContext = SpringApplication.run(SpringInitializer.class, new String[0]);
        logger.info("Context created. SpringInitializer: {}, ClassLoader: {}, ApplicationContext: {}", SpringInitializer.class, SpringApplication.class.getClassLoader(), applicationContext);
        return applicationContext;
    }
    
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
