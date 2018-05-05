package com.apache.a4javadoc.plugin;

import java.util.List;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;

/** 
 * Stateful singleton creates an instance of a {@link PluginManager}, creates and holds the plugins in a {@link #methodStateRecorders}.
 * @author Kyrylo Semenko
 */
public class AgentPluginManager {
    private static final Logger logger = LoggerFactory.getLogger(AgentPluginManager.class);
    
    /** Plugins of the module */
    private List<MethodStateRecorder> methodStateRecorders;
    
    private static AgentPluginManager instance;
    
    /**
     * A faktory of the singleton instance of the class
     * @return the singleton
     */
    public static AgentPluginManager getInstance() {
        if (instance == null) {
            instance = new AgentPluginManager();
        }
        return instance;
    }
    
    /** Create an instance of a {@link PluginManager} */
    private AgentPluginManager() {
        final PluginManager pluginManager = new DefaultPluginManager();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        methodStateRecorders = pluginManager.getExtensions(MethodStateRecorder.class);
        logger.debug("Initialized");
    }

    /** @return The {@link AgentPluginManager#methodStateRecorders} field */
    public List<MethodStateRecorder> getMethodStateRecorders() {
        return methodStateRecorders;
    }

}
