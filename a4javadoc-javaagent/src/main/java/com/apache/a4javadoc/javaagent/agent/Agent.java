package com.apache.a4javadoc.javaagent.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.pf4j.AbstractPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.exception.AppRuntimeException;

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
    /** Product name */
    public static final String A4JAVADOC = "a4javadoc";

    static final String CANNOT_CREATE_PLUGINS_DIRECTORY = "Cannot create plugins directory '";

    static final String CANNOT_FIND = "Cannot find '";

    static final String PLUGINS_DIRECTORY_DEFAULT_NAME = "plugins";

    private static final Logger logger = LoggerFactory.getLogger(Agent.class);

    /** The path of the folder where plugins are installed. See a {@link AbstractPluginManager#getPluginsRoot()} method. */
    static final String PF4J_PLUGINS_DIR = "pf4j.pluginsDir";

    static final String JAVAAGENT_ARGS_PREFIX = "-javaagent:";
    
    /**
     * Create a new {@link Agent} instance and call {@link #doPremain(String, Instrumentation, Default)}
     * @param args does not used
     * @param instrumentation see the {@link Instrumentation} javaDoc
     */
    public static void premain(String args, Instrumentation instrumentation) {
        Agent agent = new Agent();
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
        List<String> arguments = runtimeMxBean.getInputArguments();
        logger.info("JVM arguments: {}", arguments);
        
        initPluginsDirectory(arguments);
        
        final AsmVisitorWrapper methodsVisitor = Advice
                .to(MethodInterceptor.class)
                .on(MethodsMatcher.getInstance().and(ElementMatchers.isMethod()));
        
        final AsmVisitorWrapper constructorsVisitor = Advice
                .to(ConstructorInterceptor.class)
                .on(MethodsMatcher.getInstance().and(ElementMatchers.isConstructor().or(ElementMatchers.isTypeInitializer())));
        
        agentBuilderDefault
//            .with(AgentBuilder.Listener.WithErrorsOnly.StreamWriting.toSystemError())
            .type(new ClassesMatcher())
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
     * In case when {@link System} property {@link #PF4J_PLUGINS_DIR} is not defined, the method sets the property.<br>
     * The value of the property will be set to a directory {@link #PLUGINS_DIRECTORY_DEFAULT_NAME} inside the directory where a4javadoc-javaagent.jar is located. 
     */
    void initPluginsDirectory(List<String> arguments) {
        if (System.getProperty(PF4J_PLUGINS_DIR) == null) {
            File javaagentParentDirectory = findJavaagentDir(arguments);
            File pluginsDirectory = new File(javaagentParentDirectory, PLUGINS_DIRECTORY_DEFAULT_NAME);
            if (!pluginsDirectory.exists()) {
                boolean isDirCreated = pluginsDirectory.mkdirs();
                if (isDirCreated) {
                    logger.info("Plugins directory created: '{}'", pluginsDirectory.getAbsolutePath());
                } else {
                    throw new AppRuntimeException(CANNOT_CREATE_PLUGINS_DIRECTORY + pluginsDirectory.getAbsolutePath() + "'");
                }
            } else {
                logger.info("Plugins directory already exists: '{}'", pluginsDirectory.getAbsolutePath());
            }
            System.setProperty(PF4J_PLUGINS_DIR, pluginsDirectory.getAbsolutePath());
        }
    }
    
    /**
     * Parse JVM arguments and find out *javaagent*.jar directory.<br>
     * @param arguments for example <pre>-javaagent:c:\Users\Joe\temp\a4javadoc-javaagent-0.0.1.jar ...</pre>
     * @return for example <b>c:\Users\Joe\temp</b>
     */
    File findJavaagentDir(List<String> arguments) {
        for (String arg : arguments) {
            if (arg.startsWith(JAVAAGENT_ARGS_PREFIX) && arg.contains(A4JAVADOC)) {
                int beginIndex = JAVAAGENT_ARGS_PREFIX.length();
                File file = new File(arg.substring(beginIndex));
                return file.getParentFile();
            }
        }
        throw new AppRuntimeException(CANNOT_FIND + JAVAAGENT_ARGS_PREFIX + "' in arguments '" + arguments + "'");
    }

}
