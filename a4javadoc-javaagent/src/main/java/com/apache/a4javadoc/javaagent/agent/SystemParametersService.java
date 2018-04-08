package com.apache.a4javadoc.javaagent.agent;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton. Parser for {@link System} parameters. Stateful object contains {@link #includePackages} and {@link #excludePackages}.
 * @author Kyrylo Semenko
 */
public class SystemParametersService {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemParametersService.class);
    
    /** JVM argument contains package names which have to be included in instrumentation */
    private static final String JVM_ARG_INCLUDE_PACKAGES = "includePackages";
    
    /** JVM argument contains package names which have to be excluded from instrumentation */
    private static final String JVM_ARG_EXCLUDE_PACKAGES = "excludePackages";
    
    private static final String PACKAGES_SEPARATOR = ",";
    
    /** Strings obtained from {@link #JVM_ARG_INCLUDE_PACKAGES} */
    private Set<String> includePackages = null;
    
    /** Strings obtained from {@link #JVM_ARG_EXCLUDE_PACKAGES} */
    private Set<String> excludePackages = null;
    
    private static SystemParametersService instance;
    
    /** The static faktory */
    public static SystemParametersService getInstance() {
        if (instance == null) {
            instance = new SystemParametersService();
        }
        return instance;
    }
    
    /** The private empty constructor */
    private SystemParametersService() {
        // empty
    }
    
    /**
     * If the {@link #includePackages} is null, fill it out.
     * @return {@link #includePackages}
     */
    public Set<String> getIncludePackages() {
        if (includePackages == null) {
            prepareIncludes();
        }
        return includePackages;
    }
    
    /**
     * If the {@link #excludePackages} is null, fill it out.
     * @return {@link #excludePackages}
     */
    public Set<String> getExcludePackages() {
        if (excludePackages == null) {
            prepareExcludes();
        }
        return excludePackages;
    }
    
    /** If the {@link #includePackages} is null, fill it out. Else do nothing. */
    private void prepareIncludes() {
        String includePackagesString = System.getProperty(JVM_ARG_INCLUDE_PACKAGES);
        logger.info("-DincludePackages: '{}'", includePackagesString);
        if (includePackagesString != null) {
            includePackages = new HashSet<>(Arrays.asList(includePackagesString.split(PACKAGES_SEPARATOR)));
        } else {
            includePackages = Collections.emptySet();
        }
    }

    /** If the {@link #excludePackages} is null, fill it out. Else do nothing. */
    private void prepareExcludes() {
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
