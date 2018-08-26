package com.apache.a4javadoc.javaagent.mapper;

/**
 * Provides configuration parameters. 
 * @author Kyrylo Semenko
 */
public class ConfigService {

    private static ConfigService instance;
    
    /** Maximum plunging depth of {@link ContainerType}, beginning from 1. Default value is 3. */
    private int maxDepth = 3;
    
    /**
     * The empty constructor.
     */
    private ConfigService() {
        // empty
    }

    /**
     * @return the {@link IdentifierService} singleton.
     */
    public static ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }

    /** @return The {@link ConfigService#maxDepth} field */
    public int getMaxDepth() {
        return maxDepth;
    }

    /** @param maxDepth see the {@link ConfigService#maxDepth} field */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}
