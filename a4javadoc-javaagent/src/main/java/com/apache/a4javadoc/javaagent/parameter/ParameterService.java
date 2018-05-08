package com.apache.a4javadoc.javaagent.parameter;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.exception.AppRuntimeException;

/**
 * Stateful singleton with that holds application parameters
 * @author Kyrylo Semenko
 */
public class ParameterService {
    static final String THE_FILE_COULD_NOT_BE_FOUND = "The file could not be found: ";

    private static final Logger logger = LoggerFactory.getLogger(ParameterService.class);
    
    private static ParameterService instance;

    /** @return a {@link ParameterService#instance} singleton */
    public static ParameterService getInstance() {
        if (instance == null) {
            instance = new ParameterService();
        }
        return instance;
    }

    /** Configuration properties loaded from a file */
    private Properties properties;
    
    /**
     * An empty private constructor
     */
    private ParameterService() {
        // an empty
    }

    /** 
     * Load a *.properties configuration file.<br>
     * <ul>
     *     <li>Try to load the file from an absolute path</li>
     *     <li>If the file not found, try to load the file from a relative path relatively to a4javadoc-javaagent.jar</li>
     *     <li>If the file not found throw an exception</li>
     *     <li>Set the loaded properties to the {@link #properties} field and return it for test purposes</li>
     *  </ul>
     * @param filePath configuration file absolute or relative path
     * @param javaagentParentDirectory the folder where a4javadoc-javaagent.jar file placed
     * @return {@link #properties} field for test purposes
     */
    public Properties loadParameters(String filePath, File javaagentParentDirectory) {
           try {
               logger.info("args: {}", filePath);
               this.properties = new Properties();
               final Path path = Paths.get(filePath);
               if (Files.exists(path)) {
                   properties.load(new FileInputStream(filePath));
                   return this.properties;
               }
               
               File file = new File(javaagentParentDirectory, filePath);
               if (!file.exists()) {
                   throw new AppRuntimeException(THE_FILE_COULD_NOT_BE_FOUND + file.getAbsolutePath());
               }
               
               properties.load(new FileInputStream(file));
               return this.properties;
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * Find out a property from {@link #properties}. Throw {@link AppRuntimeException} if the property could not be found.
     * @param propertyName the property key
     * @return the property value
     */
    public String getProperty(String propertyName) {
        if (properties == null) {
            throw new AppRuntimeException("'properties' is null.");
        }
        if (!properties.containsKey(propertyName)) {
            throw new AppRuntimeException("The property with key '" + propertyName + "' could not be found");
        }
        return properties.getProperty(propertyName);
    }

}
