package com.apache.a4javadoc.javaagent.agent;

import java.security.ProtectionDomain;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.agent.builder.AgentBuilder.RawMatcher;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.utility.JavaModule;

/**
 * Filters classes for instrumentation in {@link Agent}, see the {@link #matches(TypeDescription, ClassLoader, JavaModule, Class, ProtectionDomain)} method.
 * @author Kyrylo Semenko
 */
public class CustomClassesMatcher implements RawMatcher {
    private static final Logger logger = LoggerFactory.getLogger(CustomClassesMatcher.class);
    
    /** En empty constructor with a log message */
    public CustomClassesMatcher() {
        logger.info("Construction of CustomClassesMatcher started");
    }

    /** 
     * Filters classes defined in {@link SystemParametersService#getIncludeNames()} and {@link SystemParametersService#getExcludePackages()} methods
     * @see net.bytebuddy.agent.builder.AgentBuilder.RawMatcher#matches(net.bytebuddy.description.type.TypeDescription, java.lang.ClassLoader, net.bytebuddy.utility.JavaModule, java.lang.Class, java.security.ProtectionDomain)
     */
    public boolean matches(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
        
        boolean result = startsWith(typeDescription.getName(), SystemParametersService.getInstance().getIncludeNames())
                && !startsWith(typeDescription.getName(), SystemParametersService.getInstance().getExcludePackages());
        
        if (logger.isDebugEnabled() && result) {
            logger.debug("TypeDescription matched: {}, typeDescription.getName: {}", result, typeDescription.getName());
        }
        
        return result;
    }

    /** @return true if the first parameter value starts with one of Strings from the set from the second parameter. */
    private boolean startsWith(String javaClassName, Set<String> set) {
        for (String next : set) {
            if (javaClassName.startsWith(next)) {
                return true;
            }
        }
        return false;
    }

}
