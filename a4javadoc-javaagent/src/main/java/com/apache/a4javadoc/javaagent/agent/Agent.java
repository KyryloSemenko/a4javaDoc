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
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.utility.JavaModule;

/**
 * Javaagent. It contains the {@link #premain(String, Instrumentation)} method. See mechanism described in the {@link Instrumentation}.<br>
 * The class also contains the {@link #main(String[])} method. 
 * @author Kyrylo Semenko
 */
public class Agent {
    private static final String PLUGINS_DIRECTORY_DEFAULT_NAME = "plugins";

    private static final Logger logger = LoggerFactory.getLogger(Agent.class);

    /** The path of the folder where plugins are installed. See a {@link AbstractPluginManager#getPluginsRoot()} method. */
    private static final String PF4J_PLUGINS_DIR = "pf4j.pluginsDir";

    private static final String JAVAAGENT_ARGS_PREFIX = "-javaagent:";
    
    /**
     * Obtains a {@link MethodInterceptor} instance from the Spring container and add it to the {@link Instrumentation}.
     * @param args not used
     * @param instrumentation see the {@link Instrumentation} javaDoc
     */
    public static void premain(String args, Instrumentation instrumentation) {

        logger.info("Javaagent classpath root: {}", (new File("")).getAbsolutePath());
        logger.info("Premain args: {}", args);
        
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        logger.info("JVM arguments: {}", arguments);
        
        initPluginsDirectory(arguments);
        
        new AgentBuilder.Default()
        .type(new CustomClassesMatcher())
        .transform(new AgentBuilder.Transformer() {
            public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                CustomMethodsMatcher<MethodDescription> customMethodsMatcher = new CustomMethodsMatcher<>();
                logger.trace("customMethodsMatcher: {}", customMethodsMatcher);
                
                return builder.method(customMethodsMatcher)
                        .intercept(MethodDelegation.to(MethodInterceptor.getInstance()));
            }
        })
        .installOn(instrumentation);
        
        logger.info("Premain finished");
    }

    /**
     * In case when {@link System} property {@link #PF4J_PLUGINS_DIR} is not defined, the method sets the property.<br>
     * The value of the property will be set to a directory {@link #PLUGINS_DIRECTORY_DEFAULT_NAME} inside the directory where a4javadoc-javaagent.jar is located. 
     */
    private static void initPluginsDirectory(List<String> arguments) {
        if (System.getProperty(PF4J_PLUGINS_DIR) == null) {
            File javaagentParentDirectory = findJavaagentDir(arguments);
            File pluginsDirectory = new File(javaagentParentDirectory, PLUGINS_DIRECTORY_DEFAULT_NAME);
            if (!pluginsDirectory.exists()) {
                boolean isDirCreated = pluginsDirectory.mkdirs();
                if (isDirCreated) {
                    logger.info("Plugins directory created: '{}'", pluginsDirectory.getAbsolutePath());
                } else {
                    throw new AppRuntimeException("Can not create plugins directory '" + pluginsDirectory.getAbsolutePath() + "'");
                }
            } else {
                logger.info("Plugins directory: '{}'", pluginsDirectory.getAbsolutePath());
            }
            System.setProperty(PF4J_PLUGINS_DIR, pluginsDirectory.getAbsolutePath());
        }
    }
    
    /**
     * Parse JVM arguments and find out *javaagent*.jar directory.<br>
     * @param arguments for example <pre>-javaagent:c:\Users\Joe\temp\a4javadoc-javaagent-0.0.1.jar ...</pre>
     * @return for example <b>c:\Users\Joe\temp</b>
     */
    private static File findJavaagentDir(List<String> arguments) {
        for (String arg : arguments) {
            if (arg.contains(JAVAAGENT_ARGS_PREFIX)) {
                int beginIndex = JAVAAGENT_ARGS_PREFIX.length();
                File file = new File(arg.substring(beginIndex));
                return file.getParentFile();
            }
        }
        throw new AppRuntimeException("Can not find '" + JAVAAGENT_ARGS_PREFIX + "' in arguments '" + arguments + "'");
    }

    /**
     * Print out information about the jar and do nothing else.
     */
    public static void main(String[] args) {
        logger.info("The jar is not runnable. See readme on https://github.com/KyryloSemenko/a4javaDoc");
    }
}
