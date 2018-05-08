package com.apache.a4javadoc.javaagent.parameter;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.apache.a4javadoc.exception.AppRuntimeException;

/** 
 * @author Kyrylo Semenko
 */
public class ParameterServiceTest {
    @SuppressWarnings("javadoc")
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    @SuppressWarnings("javadoc")
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.parameter.ParameterService#loadParameters(String, File)}.
     * @throws Exception 
     */
    @Test
    public void testLoadParametersAbsolute() throws Exception {
        File jarFile = temporaryFolder.newFile("a4javadoc-javaagent.jar");
        File propertiesFile = temporaryFolder.newFile("app.properties");
        ParameterService parameterService = spy(ParameterService.getInstance());
        Properties properties = parameterService.loadParameters(propertiesFile.getAbsolutePath(), jarFile.getParentFile());
        assertNotNull(properties);
    }
    
    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.parameter.ParameterService#loadParameters(String, File)}.
     * @throws Exception 
     */
    @Test
    public void testLoadParametersRelative() throws Exception {
        File jarFile = temporaryFolder.newFile("a4javadoc-javaagent.jar");
        temporaryFolder.newFile("app.properties");
        ParameterService parameterService = spy(ParameterService.getInstance());
        Properties properties = parameterService.loadParameters("app.properties", jarFile.getParentFile());
        assertNotNull(properties);
    }
    
    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.parameter.ParameterService#loadParameters(String, File)}.
     * @throws Exception 
     */
    @Test
    public void testLoadParametersError() throws Exception {
        File jarFile = temporaryFolder.newFile("a4javadoc-javaagent.jar");
        temporaryFolder.newFile("app.properties");
        ParameterService parameterService = spy(ParameterService.getInstance());
        expectedException.expect(AppRuntimeException.class);
        expectedException.expectMessage(ParameterService.THE_FILE_COULD_NOT_BE_FOUND);
        parameterService.loadParameters("nonexisting.file", jarFile.getParentFile());
    }

}
