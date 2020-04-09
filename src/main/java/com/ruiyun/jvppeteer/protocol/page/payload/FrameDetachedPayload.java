package com.ruiyun.jvppeteer.protocol.page.payload;

/**
 * Fired when frame has been detached from its parent.
 */
public class FrameDetachedPayload {

    /**
     * Id of the frame that has been detached.
     */
    private String frameId;

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }
}
