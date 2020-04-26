package com.ruiyun.jvppeteer.protocol.dom;

import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(WaitTask.class);

    private boolean terminated;

    private ElementHandle promise;

    public WaitTask(DOMWorld domWorld, String pageFunction, PageEvaluateType type, String title, String polling, int timeout, Object... args) {
    }

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

    public ElementHandle getPromise() {
        return promise;
    }

    public void setPromise(ElementHandle promise) {
        this.promise = promise;
    }
}
