package com.apache.a4javadoc.javaagent.agent.namefilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton. Parser for {@link System} parameters. Stateful object contains {@link #includeNames} and {@link #excludeNames} to be instrumented.
 * @author Kyrylo Semenko
 */
public class NameFilterService {
    
    private static final Logger logger = LoggerFactory.getLogger(NameFilterService.class);
    
    /**
     * The {@link System} property contains the resource names which have to be instrumented.<br>
     * Resource names should be separated by '{@value #RULE_SEPARATOR}' and can be wildcarded, see a {@link FilenameUtils#wildcardMatch(String, String)} method javaDoc.<br>
     * The each method in an examined application has a name, for example <i>com.foo.ThreadExample.run()</i> or <i>com.foo.ThreadExample.printMessage(java.lang.String, java.io.InputStream)</i><br>
     * A person who runs a a4javadoc javaagent should define which methods will be instrumented in a JVM argument, for example <pre>... -javaagent=c:\Users\Joe\temp\a4javadoc-javaagent.jar -Da4javadoc.include=com.foo*|org.foo*|*.foo.*.MyClass* ...</pre><br>
     * 
     * Examples:<br>
     * <b>com.foo.*</b> - All methods in a com.foo package<br>
     * <b>com.foo*</b> - All methods in a com package and sub packages started with foo, for example com.food<br>
     * <b>com.*.foo*</b> - All methods in a com.anything.foo packages<br>
     * <b>*.ExampleClass*</b> - All methods of an ExampleClass classes from all packages<br>
     * <b>com.*.ExampleClass*</b> - All methods in a ExampleClass classes from all sub packages in a <i>com</i> package<br>
     * <b>com.*.ExampleClass.get*</b> - All getters in all ExampleClass classes from all sub packages in a <i>com</i> package<br>
     * <b>com.foo.ExampleClass.method(java.lang.String,long,java.lang.StringBuilder)</b> - the one defined method only.
     */
    static final String SYSTEM_PROPERTY_INCLUDE_NAMES = "a4javadoc.include";
    
    /**
     * The {@link System} property contains the resource names which have to be excluded from instrumentation.<br>
     * Description of the property syntax see in {@link #SYSTEM_PROPERTY_INCLUDE_NAMES}<br>
     * An example of JVM args:<br>
     * <pre>... -javaagent=c:\Users\Joe\temp\a4javadoc-javaagent.jar -Da4javadoc.include=com.foo*|com.bar.method(java.lang.String,int) -Da4javadoc.exclude=*.setPassword*|*.getPassword()|com.foo.secure* ...</pre><br>
     */
    static final String SYSTEM_PROPERTY_EXCLUDE_NAMES = "a4javadoc.exclude";
    
    private static final String RULE_SEPARATOR = "|";
    
    /** Strings obtained from {@link #SYSTEM_PROPERTY_INCLUDE_NAMES} */
    private Set<String> includeNames = null;
    
    /** Strings obtained from {@link #SYSTEM_PROPERTY_EXCLUDE_NAMES} */
    private Set<String> excludeNames = null;
    
    private static NameFilterService instance;
    
    /**
     * The static faktory 
     * @return a {@link NameFilterService} single instance
     * 
     */
    public static NameFilterService getInstance() {
        if (instance == null) {
            instance = new NameFilterService();
        }
        return instance;
    }
    
    /** The private empty constructor */
    private NameFilterService() {
        // empty
    }
    
    /**
     * If the {@link #includeNames} is null, fill it out.
     * @return {@link #includeNames}
     */
    private Set<String> getIncludeNames() {
        if (includeNames == null) {
            prepareIncludes();
        }
        return includeNames;
    }
    
    /**
     * If the {@link #excludeNames} is null, fill it out.
     * @return {@link #excludeNames}
     */
    private Set<String> getExcludeNames() {
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
            includeNames = new HashSet<String>(Arrays.asList(includeNamesString.split(Pattern.quote(RULE_SEPARATOR))));
        } else {
            includeNames = Collections.emptySet();
        }
    }

    /** If the {@link #excludeNames} is null, fill it out. Else do nothing. */
    private void prepareExcludes() {
        String excludeNamesString = System.getProperty(SYSTEM_PROPERTY_EXCLUDE_NAMES);
        logger.info("-D{}: '{}'", SYSTEM_PROPERTY_EXCLUDE_NAMES, excludeNamesString);
        excludeNames = new HashSet<String>();
        if (excludeNamesString != null) {
            excludeNames = new HashSet<String>(Arrays.asList(excludeNamesString.split(Pattern.quote(RULE_SEPARATOR))));
        } else {
            excludeNames = Collections.emptySet();
        }
    }

    /**
     * Decides if the method or class has to be processed.
     * See {@link #SYSTEM_PROPERTY_INCLUDE_NAMES} and {@link #SYSTEM_PROPERTY_EXCLUDE_NAMES} description
     * @param name of a class or a method for analyzing
     * @return true if the name has to be accepted and processed
     */
    public boolean matches(String name) {
        boolean include = false;
        for (String includeName : getIncludeNames()) {
            if (FilenameUtils.wildcardMatch(name, includeName)) {
                include = true;
                break;
            }
        }
        if (!include) {
            return false;
        }
        for (String excludeName : getExcludeNames()) {
            if (FilenameUtils.wildcardMatch(name, excludeName)) {
                return false;
            }
        }
        return true;
    }

}
