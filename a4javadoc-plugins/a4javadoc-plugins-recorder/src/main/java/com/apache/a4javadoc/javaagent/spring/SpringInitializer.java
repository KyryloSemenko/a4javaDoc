package com.apache.a4javadoc.javaagent.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.apache.a4javadoc.javaagent.mapper.ObjectMapperA4j;

/** 
 * @author Kyrylo Semenko
 */
//@Configuration
@SpringBootApplication
//@EnableAutoConfiguration
@ComponentScan("com.apache.a4javadoc.javaagent")
public class SpringInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringInitializer.class);
    
    private static final ApplicationContext applicationContext = createSpringContext();
    
    /**
     * An empty main method for development purposes only.
     */
    public static void main(String[] args) {
        System.out.println("Main started");
        createSpringContext();
        logger.info("ObjectMapperA4j: {}", applicationContext.getBean(ObjectMapperA4j.class).toString());
        System.out.println("ObjectMapperA4j: " + applicationContext.getBean(ObjectMapperA4j.class).toString());

    }
    
    private static ApplicationContext createSpringContext() {
        logger.info("Creation of Spring context. SpringInitializer: {}, ClassLoader: {}", SpringInitializer.class, SpringApplication.class.getClassLoader());
        SpringApplication springApplication = new SpringApplication(SpringInitializer.class);
        springApplication.setLogStartupInfo(false);
        springApplication.setBannerMode(Mode.OFF);
        ApplicationContext applicationContext = springApplication.run(new String[0]);
        
//        ApplicationContext applicationContext = SpringApplication.run(SpringInitializer.class, new String[0]);
        logger.info("Context created. SpringInitializer: {}, ClassLoader: {}, ApplicationContext: {}", SpringInitializer.class, SpringApplication.class.getClassLoader(), applicationContext);
        return applicationContext;
    }
    
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
