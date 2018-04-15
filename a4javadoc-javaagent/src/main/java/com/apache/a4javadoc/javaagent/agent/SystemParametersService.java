package com.apache.a4javadoc.javaagent.agent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton. Parser for {@link System} parameters. Stateful object contains {@link #includeNames} and {@link #excludeNames} to be instrumented.
 * @author Kyrylo Semenko
 */
public class SystemParametersService {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemParametersService.class);
    
    /**
     * The {@link System} property contains the method names which have to be instrumented.<br>
     * Patterns are separated by comma ','.<br>
     * The each method in a examined application has a name, for example <i>com.foo.ThreadExample.run()</i> or <i>com.foo.ThreadExample.printMessage(java.lang.String, java.io.InputStream)</i><br>
     * Operator of a a4javadoc javaagent can define which methods will be instrumented in a JVM argument, for example <pre>... -javaagent=c:\Users\Joe\temp\a4javadoc-javaagent.jar -Da4javadoc.include=com.foo,org.foo,*.foo.*.MyClass ...</pre><br>
     * Examples:<br>
     * <b>com.foo.</b> - All methods in a com.foo package<br>
     * <b>com.foo</b> - All methods in a com package and sub packages started with foo, for example com.food<br>
     * <b>com.*.foo</b> - All methods in a com.anything.foo packages<br>
     * <b>*.ExampleClass</b> - All methods in a ExampleClass classes from all packages<br>
     * <b>com.*.ExampleClass</b> - All methods in a ExampleClass classes from all sub packages in a <i>com</i> package<br>
     * <b>com.*.ExampleClass.get*</b> - All getters in all ExampleClass classes from all sub packages in a <i>com</i> package<br>
     * <b>com.foo.ExampleClass.method(java.lang.String, long, java.lang.StringBuilder)</b> - the one defined method only.
     */
    private static final String SYSTEM_PROPERTY_INCLUDE_NAMES = "a4javadoc.include";
    
    /**
     * The {@link System} property contains the method names which have to be excluded from instrumentation.<br>
     * Description of the property syntax see in {@link #SYSTEM_PROPERTY_INCLUDE_NAMES}<br>
     * An example of JVM args:<br>
     * <pre>... -javaagent=c:\Users\Joe\temp\a4javadoc-javaagent.jar -Da4javadoc.include=com.foo -Da4javadoc.exclude=*.setPassword(java.lang.String),*.getPassword(),org.foo.secure ...</pre><br>
     */
    private static final String SYSTEM_PROPERTY_EXCLUDE_NAMES = "a4javadoc.exclude";
    
    private static final String PACKAGES_SEPARATOR = ",";
    
    /** Strings obtained from {@link #SYSTEM_PROPERTY_INCLUDE_NAMES} */
    private Set<String> includeNames = null;
    
    /** Strings obtained from {@link #SYSTEM_PROPERTY_EXCLUDE_NAMES} */
    private Set<String> excludeNames = null;
    
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
     * If the {@link #includeNames} is null, fill it out.
     * @return {@link #includeNames}
     */
    public Set<String> getIncludeNames() {
        if (includeNames == null) {
            prepareIncludes();
        }
        return includeNames;
    }
    
    /**
     * If the {@link #excludeNames} is null, fill it out.
     * @return {@link #excludeNames}
     */
    public Set<String> getExcludePackages() {
        if (excludeNames == null) {
            prepareExcludes();
        }
        return excludeNames;
    }
    
    /** If the {@link #includeNames} is null, fill it out. Else do nothing. */
    private void prepareIncludes() {
        String includeNamesString = System.getProperty(SYSTEM_PROPERTY_INCLUDE_NAMES);
        logger.info("-D{}: '{}'", SYSTEM_PROPERTY_INCLUDE_NAMES, includeNamesString);
        if (includeNamesString != null) {
            includeNames = new HashSet<>(Arrays.asList(includeNamesString.split(PACKAGES_SEPARATOR)));
        } else {
            includeNames = Collections.emptySet();
        }
    }

    /** If the {@link #excludeNames} is null, fill it out. Else do nothing. */
    private void prepareExcludes() {
        String excludeNamesString = System.getProperty(SYSTEM_PROPERTY_EXCLUDE_NAMES);
        logger.info("-D{}: '{}'", SYSTEM_PROPERTY_EXCLUDE_NAMES, excludeNamesString);
        excludeNames = new HashSet<>();
        if (excludeNamesString != null) {
            excludeNames = new HashSet<>(Arrays.asList(excludeNamesString.split(PACKAGES_SEPARATOR)));
        } else {
            excludeNames = Collections.emptySet();
        }
    }

}
