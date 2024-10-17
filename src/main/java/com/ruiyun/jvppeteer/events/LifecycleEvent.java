package com.ruiyun.jvppeteer.events;

import java.math.BigDecimal;

/**
 * Fired for top level page lifecycle events such as navigation, load, paint, etc.
 */
public class LifecycleEvent {

    /**
     * Id of the frame.
     */
    private String frameId;
    /**
     * Loader identifier. Empty string if the request is fetched from worker.
     */
    private String loaderId;

    private String name;

   private BigDecimal timestamp;

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getLoaderId() {
        return loaderId;
    }

    public void setLoaderId(String loaderId) {
        this.loaderId = loaderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigDecimal timestamp) {
        this.timestamp = timestamp;
    }
}
