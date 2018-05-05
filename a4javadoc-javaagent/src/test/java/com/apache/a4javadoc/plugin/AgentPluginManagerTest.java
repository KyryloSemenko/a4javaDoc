package com.apache.a4javadoc.plugin;

import static org.junit.Assert.*;

import org.junit.Test;

/** 
 * @author Kyrylo Semenko
 */
public class AgentPluginManagerTest {

    /**
     * Test method for {@link AgentPluginManager}
     */
    @Test
    public void test() {
        AgentPluginManager agentPluginManager = AgentPluginManager.getInstance();
        assertTrue(agentPluginManager.getMethodStateRecorders().isEmpty());
    }

}
