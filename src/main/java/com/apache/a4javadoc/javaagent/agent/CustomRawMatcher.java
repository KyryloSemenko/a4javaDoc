package com.apache.a4javadoc.javaagent.agent;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.bytebuddy.agent.builder.AgentBuilder.RawMatcher;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.utility.JavaModule;

/**
 * Filters classes for instrumentation in {@link Agent}, see the {@link #matches(TypeDescription, ClassLoader, JavaModule, Class, ProtectionDomain)} method.
 * @author Kyrylo Semenko
 */
@Component
public class CustomRawMatcher implements RawMatcher {
    private static final Logger logger = LoggerFactory.getLogger(CustomRawMatcher.class);
    
    /** JVM argument contains package names which have to be included in instrumentation */
    private static final String JVM_ARG_INCLUDE_PACKAGES = "includePackages";
    
    /** JVM argument contains package names which have to be excluded from instrumentation */
    private static final String JVM_ARG_EXCLUDE_PACKAGES = "excludePackages";
    
    private static final String PACKAGES_SEPARATOR = ",";
    
    /** Strings obtained from {@link #JVM_ARG_INCLUDE_PACKAGES} */
    private Set<String> includePackages = null;
    
    /** Strings obtained from {@link #JVM_ARG_EXCLUDE_PACKAGES} */
    private Set<String> excludePackages = null;

    /** 
     * Filters classes defined in -DincludePackages and -DexcludePackages.
     * @see net.bytebuddy.agent.builder.AgentBuilder.RawMatcher#matches(net.bytebuddy.description.type.TypeDescription, java.lang.ClassLoader, net.bytebuddy.utility.JavaModule, java.lang.Class, java.security.ProtectionDomain)
     */
    public boolean matches(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
        prepareIncludes();
        prepareExcludes();
        return startsWith(typeDescription.getName(), includePackages) && !startsWith(typeDescription.getName(), excludePackages);
    }

    /** If the {@link #excludePackages} is null, fill it out. Else do nothing. */
    private void prepareExcludes() {
        if (excludePackages == null) {
            String excludePackagesString = System.getProperty(JVM_ARG_EXCLUDE_PACKAGES);
            logger.info("-DexcludePackages: '{}'", excludePackagesString);
            excludePackages = new HashSet<>();
            if (excludePackagesString != null) {
                excludePackages = new HashSet<>(Arrays.asList(excludePackagesString.split(PACKAGES_SEPARATOR)));
            } else {
                excludePackages = Collections.emptySet();
            }
        }
    }

    /** If the {@link #includePackages} is null, fill it out. Else do nothing. */
    private void prepareIncludes() {
        if (includePackages == null) {
            String includePackagesString = System.getProperty(JVM_ARG_INCLUDE_PACKAGES);
            logger.info("-DincludePackages: '{}'", includePackagesString);
            if (includePackagesString != null) {
                includePackages = new HashSet<>(Arrays.asList(includePackagesString.split(PACKAGES_SEPARATOR)));
            } else {
                includePackages = Collections.emptySet();
            }
        }
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
