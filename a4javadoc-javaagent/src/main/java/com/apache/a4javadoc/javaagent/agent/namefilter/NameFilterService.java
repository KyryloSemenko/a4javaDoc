package com.apache.a4javadoc.javaagent.agent.namefilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.javaagent.agent.Agent;
import com.apache.a4javadoc.javaagent.parameter.ParameterService;

/**
 * Singleton. Parser for {@link System} parameters. Stateful object contains {@link #includeNames} and {@link #excludeNames} to be instrumented.
 * @author Kyrylo Semenko
 */
public class NameFilterService {
    
    private static final Logger logger = LoggerFactory.getLogger(NameFilterService.class);
    
    /**
     * The property key mapped to the resource names which have to be instrumented.<br>
     * Resource names should be separated by '{@value #RULE_SEPARATOR}' and can be wildcarded, see a {@link FilenameUtils#wildcardMatch(String, String)} method javaDoc.<br>
     * The each method in an examined application has a name, for example <i>com.foo.ThreadExample.run()</i> or <i>com.foo.ThreadExample.printMessage(java.lang.String, java.io.InputStream)</i><br>
     * A person who runs a a4javadoc javaagent should define which methods will be instrumented in a properties file, for example in JVM command line <pre>... -javaagent:c:\Users\Joe\temp\a4javadoc-javaagent.jar=myConfig.properties ...</pre><br>
     * or <pre>... -javaagent:c:\Users\Joe\temp\a4javadoc-javaagent.jar=/etc/a4javadoc/myApp/myConfig.properties ...</pre><br>
     * Example of properties file content 
     * <pre>
     * a4javadoc.include=com.foo*|org.foo*|*.foo.*.MyClass*
     * a4javadoc.exclude=*.setPassword*|*.getPassword()|com.foo.secure*
     * </pre><br>
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
    public static final String PROPERTY_INCLUDE_NAMES = Agent.A4JAVADOC + ".include";
    
    /**
     * The property key mapped to the resource names which have to be excluded from instrumentation.<br>
     * Description of the property syntax see in {@link #PROPERTY_INCLUDE_NAMES}<br>
     * An example of a configuration.properties line:<br>
     * <pre>a4javadoc.exclude=*.setPassword*|*.getPassword()|com.foo.secure* ...</pre><br>
     */
    public static final String PROPERTY_EXCLUDE_NAMES = Agent.A4JAVADOC + ".exclude";
    
    private static final String RULE_SEPARATOR = "|";
    
    /** Strings obtained from {@link #PROPERTY_INCLUDE_NAMES} */
    Set<String> includeNames = null;
    
    /** Strings obtained from {@link #PROPERTY_EXCLUDE_NAMES} */
    Set<String> excludeNames = null;
    
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
        String includeNamesString = ParameterService.getInstance().getProperty(PROPERTY_INCLUDE_NAMES);
        logger.info("Property {}: '{}'", PROPERTY_INCLUDE_NAMES, includeNamesString);
        
        if (includeNamesString != null) {
            includeNames = new HashSet<>(Arrays.asList(includeNamesString.split(Pattern.quote(RULE_SEPARATOR))));
        } else {
            includeNames = Collections.emptySet();
        }
    }

    /** If the {@link #excludeNames} is null, fill it out. Else do nothing. */
    private void prepareExcludes() {
        String excludeNamesString = ParameterService.getInstance().getProperty(PROPERTY_EXCLUDE_NAMES);
        logger.info("Property {}: '{}'", PROPERTY_EXCLUDE_NAMES, excludeNamesString);
        excludeNames = new HashSet<>();
        if (excludeNamesString != null) {
            excludeNames = new HashSet<>(Arrays.asList(excludeNamesString.split(Pattern.quote(RULE_SEPARATOR))));
        } else {
            excludeNames = Collections.emptySet();
        }
    }

    /**
     * Decides if the method or class has to be processed.
     * See {@link #PROPERTY_INCLUDE_NAMES} and {@link #PROPERTY_EXCLUDE_NAMES} description
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
