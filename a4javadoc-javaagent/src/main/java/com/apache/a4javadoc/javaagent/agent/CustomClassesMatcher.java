package com.apache.a4javadoc.javaagent.agent;

import java.security.ProtectionDomain;
import java.util.Set;

import net.bytebuddy.agent.builder.AgentBuilder.RawMatcher;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.utility.JavaModule;

/**
 * Filters classes for instrumentation in {@link Agent}, see the {@link #matches(TypeDescription, ClassLoader, JavaModule, Class, ProtectionDomain)} method.
 * @author Kyrylo Semenko
 */
public class CustomClassesMatcher implements RawMatcher {

    /** 
     * Filters classes defined in -DincludePackages and -DexcludePackages.
     * @see net.bytebuddy.agent.builder.AgentBuilder.RawMatcher#matches(net.bytebuddy.description.type.TypeDescription, java.lang.ClassLoader, net.bytebuddy.utility.JavaModule, java.lang.Class, java.security.ProtectionDomain)
     */
    public boolean matches(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
        return startsWith(typeDescription.getName(), SystemParametersService.getInstance().getIncludePackages())
                && !startsWith(typeDescription.getName(), SystemParametersService.getInstance().getExcludePackages());
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
