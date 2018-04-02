package com.apache.a4javadoc.javaagent.recorder;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

/**
 * Extends {@link Plugin} and starts {@link SpringApplication}.
 * @author Kyrylo Semenko
 */
public class RecorderPlugin extends Plugin {
    
    private static final Logger logger = LoggerFactory.getLogger(RecorderPlugin.class);

    public RecorderPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        logger.info("RecorderPlugin.start()");
    }

    @Override
    public void stop() {
        logger.info("RecorderPlugin.stop()");
    }

}
