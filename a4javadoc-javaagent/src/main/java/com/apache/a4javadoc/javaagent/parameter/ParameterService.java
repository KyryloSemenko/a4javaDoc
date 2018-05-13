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

    static final String PROPERTIES_IS_NULL = "'properties' is null.";

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
    Properties properties;
    
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
                String message = THE_FILE_COULD_NOT_BE_FOUND + file.getAbsolutePath();
                logger.error(message);
                throw new AppRuntimeException(message);
            }

            properties.load(new FileInputStream(file));
            return this.properties;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new AppRuntimeException(e);
        }
    }

    /**
     * Find out a property from {@link #properties}.
     * @param propertyName the property key
     * @return the property value. Return <b>null</b> if the property could not be found.
     */
    public String getProperty(String propertyName) {
        if (properties == null) {
            logger.error(PROPERTIES_IS_NULL);
            throw new AppRuntimeException(PROPERTIES_IS_NULL);
        }
        if (!properties.containsKey(propertyName)) {
            return null;
        }
        return properties.getProperty(propertyName);
    }

}
