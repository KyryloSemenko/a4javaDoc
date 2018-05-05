package com.apache.a4javadoc.javaagent.agent;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.apache.a4javadoc.exception.AppRuntimeException;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Identified.Extendable;
import net.bytebuddy.agent.builder.AgentBuilder.Identified.Narrowable;
import net.bytebuddy.agent.builder.AgentBuilder.RawMatcher;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;

/**
 * Tests for {@link Agent}.
 * @author Kyrylo Semenko
 */
@RunWith(MockitoJUnitRunner.class)
public class AgentTest {
    
    @SuppressWarnings("javadoc")
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    
    @SuppressWarnings("javadoc")
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    @Mock
    private AgentBuilder.Default agentBuilderDefault;
    
    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.Agent#initPluginsDirectory(List)}.
     * An {@link AppRuntimeException} should be thrown, because {@link Agent#JAVAAGENT_ARGS_PREFIX} could not be found.
     */
    @Test
    public void cannotFindJavaagentTest() {
        expectedException.expect(AppRuntimeException.class);
        expectedException.expectMessage(Agent.CANNOT_FIND);
        Agent agent = spy(new Agent());
        List<String> list = Collections.emptyList();
        agent.initPluginsDirectory(list);
        verify(agent).findJavaagentDir(list);
    }
    
    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.Agent#initPluginsDirectory(List)}.
     * An {@link AppRuntimeException} should be thrown, because the scenario tries to create a new folder inside a file.
     * @throws IOException in case when creation of a new empty file failed. This file will be used in the scenario as an unwritable 'folder'.
     */
    @Test
    public void cannotCreateDirectoryTest() throws IOException {
        expectedException.expect(AppRuntimeException.class);
        expectedException.expectMessage(Agent.CANNOT_CREATE_PLUGINS_DIRECTORY);
        Agent agent = spy(new Agent());
        List<String> list = Arrays.asList(Agent.JAVAAGENT_ARGS_PREFIX + Agent.A4JAVADOC_JAR_NAME);
        when(agent.findJavaagentDir(list)).thenReturn(temporaryFolder.newFile());
        agent.initPluginsDirectory(list);
        verify(agent).findJavaagentDir(list);
    }
    
    /**
     * Test method for {@link Agent#initPluginsDirectory(List)}.
     * Find out javaagent parent directory and create a plugin directory.
     */
    @Test
    public void initPluginsDirectoryTest() {
        File tempFile = new File(temporaryFolder.getRoot(), Agent.A4JAVADOC_JAR_NAME);
        Agent agent = new Agent();
        String argument = Agent.JAVAAGENT_ARGS_PREFIX + tempFile.getAbsolutePath();
        agent.initPluginsDirectory(Arrays.asList("otherArgument=0", argument));
        File pluginDir = new File(temporaryFolder.getRoot(), Agent.PLUGINS_DIRECTORY_DEFAULT_NAME);
        
        agent.initPluginsDirectory(Arrays.asList("otherArgument=0", argument));
        
        System.clearProperty(Agent.PF4J_PLUGINS_DIR);
        agent.initPluginsDirectory(Arrays.asList("otherArgument=0", argument));

        assertTrue("Expected the directory exists: " + pluginDir.getAbsolutePath(), pluginDir.exists());
    }
    
    /**
     * Find out javaagent parent directory and create a plugin directory.
     * Test method for {@link Agent#doPremain(String, java.lang.instrument.Instrumentation, net.bytebuddy.agent.builder.AgentBuilder.Default)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void doPremainTest() {
        Agent agentSpy = spy(new Agent());
        Narrowable narrowable = mock(Narrowable.class);
        Extendable extendable = mock(Extendable.class);
        when(agentBuilderDefault.type((RawMatcher)any())).thenReturn(narrowable);
        when(narrowable.transform((Transformer)any())).thenReturn(extendable);
        when(extendable.installOn(null)).thenReturn(null);
        doNothing().when(agentSpy).initPluginsDirectory(anyList());
        agentSpy.doPremain(null, null, agentBuilderDefault);
        verify(agentBuilderDefault).type((RawMatcher) any());
        verify(narrowable).transform((Transformer) any());
        verify(extendable).installOn(null);
    }
    
    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.agent.Agent#premain(java.lang.String, java.lang.instrument.Instrumentation)}.
     * An {@link AppRuntimeException} should be thrown, because {@link Agent#JAVAAGENT_ARGS_PREFIX} could not be found. 
     */
    @Test
    public void premainFailedTest() {
        expectedException.expect(AppRuntimeException.class);
        expectedException.expectMessage(Agent.CANNOT_FIND);
        Agent.premain(null, null);
    }
    
}
