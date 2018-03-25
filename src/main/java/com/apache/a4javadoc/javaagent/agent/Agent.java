package com.apache.a4javadoc.javaagent.agent;

import java.lang.instrument.Instrumentation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

/**
 * Javaagent. It contains the {@link #premain(String, Instrumentation)} method. See mechanism described in the {@link Instrumentation}.<br>
 * The class also contains the {@link #main(String[])} method. 
 * @author Kyrylo Semenko
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan("com.apache.a4javadoc.javaagent")
public class Agent {
    private static final Logger logger = LoggerFactory.getLogger(Agent.class);

    /**
     * Obtains a {@link MethodInterceptor} instance from the Spring container and add it to the {@link Instrumentation}.
     * @param args not used
     * @param instrumentation see the {@link Instrumentation} javaDoc
     */
    public static void premain(String args, Instrumentation instrumentation) {
        if (args == null) {
            args = StringUtils.EMPTY;
        }
        final ApplicationContext context = SpringApplication.run(Agent.class, args);
        
        new AgentBuilder.Default()
        .type(context.getBean(CustomRawMatcher.class))
        .transform(new AgentBuilder.Transformer() {
            public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                return builder.method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                        .intercept(MethodDelegation.to(context.getBean(MethodInterceptor.class)));
            }
        })
        .installOn(instrumentation);
    }
    
    /**
     * Print out information about the jar and do nothing else.
     */
    public static void main(String[] args) {
        logger.info("The jar is not runnable. See readme on https://github.com/KyryloSemenko/a4javaDoc");
    }
}
