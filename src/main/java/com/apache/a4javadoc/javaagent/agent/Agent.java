package com.apache.a4javadoc.javaagent.agent;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javaagent. It contains the {@link #premain(String, Instrumentation)} method. See mechanism described in the {@link Instrumentation}.<br>
 * The class also contains the {@link #main(String[])} method. 
 * @author Kyrylo Semenko
 */
public class Agent {
    static final String JVM_ARG_EXCLUDE_PACKAGES = "excludePackages";
    static final String JVM_ARG_INCLUDE_PACKAGES = "includePackages";
    private static final String PACKAGES_SEPARATOR = ",";
    private static final Logger logger = LoggerFactory.getLogger(Agent.class);

    /**
     * Instantiate the {@link A4javaDocClassFileTransformer} and add it to the {@link Instrumentation}.
     */
    public static void premain(String args, Instrumentation instrumentation) {
        logger.info("args: '{}'", args);
        
        String includePackagesString = System.getProperty(JVM_ARG_INCLUDE_PACKAGES);
        logger.info("-DincludePackages: '{}'", includePackagesString);
        Set<String> includePackages = new HashSet<String>();
        if (includePackagesString != null) {
            includePackages = new HashSet<String>(Arrays.asList(includePackagesString.split(PACKAGES_SEPARATOR)));
        }
        
        String excludePackagesString = System.getProperty(JVM_ARG_EXCLUDE_PACKAGES);
        logger.info("-DexcludePackages: '{}'", excludePackagesString);
        Set<String> excludePackages = new HashSet<String>();
        if (excludePackagesString != null) {
            excludePackages = new HashSet<String>(Arrays.asList(excludePackagesString.split(PACKAGES_SEPARATOR)));
        }
        
        A4javaDocClassFileTransformer transformer = new A4javaDocClassFileTransformer(includePackages, excludePackages);
        instrumentation.addTransformer(transformer);
    }
    
    /**
     * Print out information about the jar and do nothing else.
     */
    public static void main(String[] args) {
        logger.info("The jar is not runnable. See readme on https://github.com/KyryloSemenko/a4javaDoc");
    }
}
