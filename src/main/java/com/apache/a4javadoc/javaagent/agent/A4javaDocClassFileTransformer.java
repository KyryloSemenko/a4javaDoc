package com.apache.a4javadoc.javaagent.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The instance of this class transforms class files, see {@link ClassFileTransformer}.
 * It contains the {@link #transform(ClassLoader, String, Class, ProtectionDomain, byte[])} method.
 * @author Kyrylo Semenko
 */
public class A4javaDocClassFileTransformer implements ClassFileTransformer {
    private static final String PACKAGE_SEPARATOR_JAVA = ".";

    private static final String PACKAGE_SEPARATOR_PATH = "/";

    private static final Logger logger = LoggerFactory.getLogger(ClassLoader.class);
    
    /** See {@link Agent#JVM_ARG_INCLUDE_PACKAGES} */
    private Set<String> includePackages;
    
    /** See {@link Agent#JVM_ARG_EXCLUDE_PACKAGES} */
    private Set<String> excludePackages;

    /** 
     * @param includePackages see {@link Agent#JVM_ARG_INCLUDE_PACKAGES}
     * @param excludePackages see {@link Agent#JVM_ARG_EXCLUDE_PACKAGES}
     */
    public A4javaDocClassFileTransformer(Set<String> includePackages, Set<String> excludePackages) {
        this.includePackages = includePackages;
        this.excludePackages = excludePackages;
    }

    /**
     * Filter classes defined in -DincludePackages and -DexcludePackages and modify their methods.
     * {@inheritDoc}
     * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader, java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
     */
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String javaClassName = className.replaceAll(PACKAGE_SEPARATOR_PATH, PACKAGE_SEPARATOR_JAVA);
        if (startsWith(javaClassName, includePackages) && !startsWith(javaClassName, excludePackages)) {
            logger.info("Matched: '{}'", javaClassName);
        }
        return classfileBuffer;
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
