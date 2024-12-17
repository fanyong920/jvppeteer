package com.ruiyun.jvppeteer.cdp.events;

/**
 * Fired when frame has stopped loading.
 */
public class FrameStartedLoadingEvent {

    /**
     * Id of the frame that has started loading.
     */
    private String frameId;

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }
}
