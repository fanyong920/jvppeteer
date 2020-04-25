package com.ruiyun.jvppeteer.protocol.dom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(WaitTask.class);

    private boolean terminated;

    public void rerun() {
    }

    public void terminate(RuntimeException e) {
        this.terminated = true;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.error("",e);
        }
        this.cleanup();
    }

    private void cleanup() {
    }
}
