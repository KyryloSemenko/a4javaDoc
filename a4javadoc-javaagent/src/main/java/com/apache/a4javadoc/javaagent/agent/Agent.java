package com.apache.a4javadoc.javaagent.agent;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.utility.JavaModule;

/**
 * Javaagent. It contains the {@link #premain(String, Instrumentation)} method. See mechanism described in the {@link Instrumentation}.<br>
 * The class also contains the {@link #main(String[])} method. 
 * @author Kyrylo Semenko
 */
public class Agent {
    private static final Logger logger = LoggerFactory.getLogger(Agent.class);

    /**
     * Obtains a {@link MethodInterceptor} instance from the Spring container and add it to the {@link Instrumentation}.
     * @param args not used
     * @param instrumentation see the {@link Instrumentation} javaDoc
     */
    public static void premain(String args, Instrumentation instrumentation) {
        new AgentBuilder.Default()
        .type(new CustomClassesMatcher())
        .transform(new AgentBuilder.Transformer() {
            public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                return builder.method(new CustomMethodsMatcher<>())
                        .intercept(MethodDelegation.to(new MethodInterceptor()));
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
