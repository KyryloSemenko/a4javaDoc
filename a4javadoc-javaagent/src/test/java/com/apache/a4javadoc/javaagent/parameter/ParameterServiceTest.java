package com.apache.a4javadoc.javaagent.parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.internal.util.reflection.Whitebox;

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
        ParameterService parameterService = ParameterService.getInstance();
        Properties properties = parameterService.loadParameters(propertiesFile.getAbsolutePath(), jarFile.getParentFile());
        assertNotNull(properties);
        Whitebox.setInternalState(parameterService, "properties", null);
    }
    
    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.parameter.ParameterService#loadParameters(String, File)}.
     * @throws Exception 
     */
    @Test
    public void testLoadParametersRelative() throws Exception {
        File jarFile = temporaryFolder.newFile("a4javadoc-javaagent.jar");
        temporaryFolder.newFile("app.properties");
        ParameterService parameterService = ParameterService.getInstance();
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
        ParameterService parameterService = ParameterService.getInstance();
        expectedException.expect(AppRuntimeException.class);
        expectedException.expectMessage(ParameterService.THE_FILE_COULD_NOT_BE_FOUND);
        parameterService.loadParameters("nonexisting.file", jarFile.getParentFile());
    }
    
    /**
     * Test method for {@link ParameterService#getProperty(String)}.
     * The case when properties not loaded.
     */
    @Test
    public void testGetPropertyNull() {
        expectedException.expect(AppRuntimeException.class);
        expectedException.expectMessage(ParameterService.PROPERTIES_IS_NULL);
        ParameterService.getInstance().getProperty("");
    }
    
    /**
     * Test method for {@link ParameterService#getProperty(String)}.
     * The case when properties loaded, but the key could not be found.
     */
    @Test
    public void testGetPropertyNotFound() {
        expectedException.expect(AppRuntimeException.class);
        expectedException.expectMessage(ParameterService.THE_PROPERTY_WITH_KEY);
        Properties properties = new Properties();
        ParameterService parameterService = ParameterService.getInstance();
        Whitebox.setInternalState(parameterService, "properties", properties);
        ParameterService.getInstance().getProperty("non existing");
    }
    
    /**
     * Test method for {@link ParameterService#getProperty(String)}.
     * Successful case.
     */
    @Test
    public void testGetProperty() {
        Properties properties = new Properties();
        properties.put("key", "value");
        ParameterService parameterService = ParameterService.getInstance();
        Whitebox.setInternalState(parameterService, "properties", properties);
        String value = ParameterService.getInstance().getProperty("key");
        assertEquals("value", value);
        Whitebox.setInternalState(parameterService, "properties", null);
    }

}
