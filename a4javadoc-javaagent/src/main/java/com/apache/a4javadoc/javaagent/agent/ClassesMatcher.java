package com.apache.a4javadoc.javaagent.agent;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.javaagent.agent.namefilter.NameFilterService;

import net.bytebuddy.agent.builder.AgentBuilder.RawMatcher;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.utility.JavaModule;

/**
 * Filters classes for instrumentation in {@link Agent}, see the {@link #matches(TypeDescription, ClassLoader, JavaModule, Class, ProtectionDomain)} method.
 * @author Kyrylo Semenko
 */
public class ClassesMatcher implements RawMatcher {
    private static final Logger logger = LoggerFactory.getLogger(ClassesMatcher.class);
    
    /** An empty constructor with a log message */
    public ClassesMatcher() {
        logger.info("Construction of ClassesMatcher started");
    }

    /** 
     * Filters classes by applying a {@link NameFilterService#matches(String)} method
     * @see net.bytebuddy.agent.builder.AgentBuilder.RawMatcher#matches(net.bytebuddy.description.type.TypeDescription, java.lang.ClassLoader, net.bytebuddy.utility.JavaModule, java.lang.Class, java.security.ProtectionDomain)
     */
    public boolean matches(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
        
        boolean result = NameFilterService.getInstance().matches(typeDescription.getName());
        
        if (logger.isDebugEnabled() && result) {
            logger.debug("TypeDescription matched: {}, typeDescription.getName: {}", result, typeDescription.getName());
        }
        
        return result;
    }
}
