package com.ruiyun.jvppeteer.protocol.page;

/**
 * Fired when frame has stopped loading.
 */
public class FrameStoppedLoadingEvent {

    /**
     * Id of the frame that has stopped loading.
     */
    private String frameId;

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }
}
