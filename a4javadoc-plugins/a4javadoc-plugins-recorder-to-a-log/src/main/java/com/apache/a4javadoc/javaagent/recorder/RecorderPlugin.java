package com.apache.a4javadoc.javaagent.recorder;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends {@link Plugin}.
 * @author Kyrylo Semenko
 */
public class RecorderPlugin extends Plugin {
    
    private static final Logger logger = LoggerFactory.getLogger(RecorderPlugin.class);

    /**
     * Call the {@link Plugin} constructor
     * @param wrapper see the {@link Plugin#Plugin(PluginWrapper)} constructor
     */
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
