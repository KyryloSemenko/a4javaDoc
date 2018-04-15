package com.apache.a4javadoc.plugin;

import java.util.List;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;

import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;

/** 
 * Stateful singleton creates an instance of a {@link PluginManager}, creates and holds the plugins in a {@link #methodStateRecorders}.
 * @author Kyrylo Semenko
 */
public class AgentPluginManager {
    
    /** Plugins of the module */
    private List<MethodStateRecorder> methodStateRecorders;
    
    private static AgentPluginManager instance;
    
    /** A faktory of the singleton instance of the class */
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
        setMethodStateRecorders(pluginManager.getExtensions(MethodStateRecorder.class));
    }

    /** @return The {@link AgentPluginManager#methodStateRecorders} field */
    public List<MethodStateRecorder> getMethodStateRecorders() {
        return methodStateRecorders;
    }

    /** See the {@link AgentPluginManager#methodStateRecorders} field */
    public void setMethodStateRecorders(List<MethodStateRecorder> methodStateRecorders) {
        this.methodStateRecorders = methodStateRecorders;
    }
    
    

}
