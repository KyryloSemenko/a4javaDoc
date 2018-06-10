package com.apache.a4javadoc.javaagent.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.pf4j.AbstractPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.exception.AppRuntimeException;
import com.apache.a4javadoc.javaagent.parameter.ParameterService;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Default;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

/**
 * Javaagent. It contains the {@link #premain(String, Instrumentation)} method. See the behavior described in the {@link Instrumentation} class.<br>
 * @author Kyrylo Semenko
 */
public class Agent {
    
    private static final String EQUALS_SIGN_DELIMITER = "=";

    /** Extension of a .jar file with a dot */
    public static final String JAR_FILE_EXTENSION = ".jar";

    private static final Logger logger = LoggerFactory.getLogger(Agent.class);
    
    /** The first path of a jar name */
    public static final String A4JAVADOC = "a4javadoc";
    
    /** The second path of a jar name */
    public static final String JAAVAGENT = "-javaagent";

    static final String CANNOT_CREATE_PLUGINS_DIRECTORY = "Cannot create plugins directory '";

    static final String CANNOT_FIND = "Cannot find '";

    static final String PLUGINS_DIRECTORY_DEFAULT_NAME = "plugins";

    /** The path of the folder where plugins are installed. See a {@link AbstractPluginManager#getPluginsRoot()} method. */
    static final String PF4J_PLUGINS_DIR = "pf4j.pluginsDir";

    static final String JAVAAGENT_ARGS_PREFIX = "-javaagent:";
    
    /**
     * Create a new {@link Agent} instance and call {@link #doPremain(String, Instrumentation, Default)}
     * @param args does not used
     * @param instrumentation see the {@link Instrumentation} javaDoc
     */
    public static void premain(String args, Instrumentation instrumentation) {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArguments = runtimeMxBean.getInputArguments();
        logger.info("JVM arguments: {}", jvmArguments);
        Agent agent = new Agent();
        ParameterService.getInstance().loadParameters(args, agent.findJavaagentDir(jvmArguments));
        AgentBuilder.Default agentBuilderDefault = new AgentBuilder.Default();
        agent.doPremain(args, instrumentation, agentBuilderDefault);
    }

    /**
     * Uses {@link AgentBuilder} for creation of {@link Transformer} and install it to the {@link Instrumentation}.
     * @param args does not used
     * @param instrumentation see the {@link Instrumentation} javaDoc
     * @param agentBuilderDefault an empty {@link AgentBuilder}
     */
    void doPremain(String args, Instrumentation instrumentation, Default agentBuilderDefault) {
        logger.info("Javaagent classpath root: {}", (new File("")).getAbsolutePath());
        logger.info("Premain args: {}", args);
        
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArguments = runtimeMxBean.getInputArguments();
        
        initPluginsDirectory(jvmArguments);
        
        final AsmVisitorWrapper methodsVisitor = Advice
                .to(MethodInterceptor.class)
                .on(MethodsMatcher.getInstance().and(ElementMatchers.isMethod()));
        
        final AsmVisitorWrapper constructorsVisitor = Advice
                .to(ConstructorInterceptor.class)
                .on(MethodsMatcher.getInstance().and(ElementMatchers.isConstructor().or(ElementMatchers.isTypeInitializer())));
        
        agentBuilderDefault
//            .with(AgentBuilder.Listener.WithErrorsOnly.StreamWriting.toSystemError())
            .type(ElementMatchers.any())
            .transform(new AgentBuilder.Transformer() {
                public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                    return builder.visit(methodsVisitor).visit(constructorsVisitor);
                }
            })
            .installOn(instrumentation);
        
        logger.info("Premain finished");
    }

    /**
     * Users of the {@link Agent} can specify a {@link #PF4J_PLUGINS_DIR} where plugins are installed, for example in java args <i>-Dpf4j.pluginsDir=/usr/bin/a4javadoc/myPlugins</i>.<br>
     * In case when {@link System} property {@link #PF4J_PLUGINS_DIR} is not set, the method sets the property.<br>
     * The value of the property will be set to a directory {@link #PLUGINS_DIRECTORY_DEFAULT_NAME} inside the directory where a4javadoc-javaagent.jar is located. 
     * @param jvmArguments JVM arguments of the application
     */
    void initPluginsDirectory(List<String> jvmArguments) {
        if (System.getProperty(PF4J_PLUGINS_DIR) == null) {
            File javaagentParentDirectory = findJavaagentDir(jvmArguments);
            File pluginsDirectory = new File(javaagentParentDirectory, PLUGINS_DIRECTORY_DEFAULT_NAME);
            if (!pluginsDirectory.exists()) {
                boolean isDirCreated = pluginsDirectory.mkdirs();
                if (isDirCreated) {
                    logger.info("Plugins directory created: '{}'", pluginsDirectory.getAbsolutePath());
                } else {
                    String message = CANNOT_CREATE_PLUGINS_DIRECTORY + pluginsDirectory.getAbsolutePath() + "'";
                    logger.error(message);
                    throw new AppRuntimeException(message);
                }
            } else {
                logger.info("Plugins directory already exists: '{}'", pluginsDirectory.getAbsolutePath());
            }
            System.setProperty(PF4J_PLUGINS_DIR, pluginsDirectory.getAbsolutePath());
        }
    }
    
    /**
     * Parse JVM arguments and find out *javaagent*.jar directory.<br>
     * @param jvmArguments for example <pre>-javaagent:c:\Users\Joe\temp\a4javadoc-javaagent-0.0.1.jar=c:\temp\a4javadoc\config.properties ...</pre>
     * @return for example <b>c:\Users\Joe\temp</b>
     */
    File findJavaagentDir(List<String> jvmArguments) {
        for (String arg : jvmArguments) {
            if (FilenameUtils.wildcardMatch(arg, JAVAAGENT_ARGS_PREFIX + "*" + A4JAVADOC + JAAVAGENT + "*" + JAR_FILE_EXTENSION + "*")) {
                int beginIndex = JAVAAGENT_ARGS_PREFIX.length();
                int endIndex = arg.indexOf(EQUALS_SIGN_DELIMITER);
                if (endIndex == -1) {
                    endIndex = arg.length();
                }
                File file = new File(arg.substring(beginIndex, endIndex));
                return file.getParentFile();
            }
        }
        String message = CANNOT_FIND + JAVAAGENT_ARGS_PREFIX + "' in arguments '" + jvmArguments + "'";
        logger.error(message);
        throw new AppRuntimeException(message);
    }

}
