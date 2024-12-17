package com.ruiyun.jvppeteer.cdp.events;

/**
 * Fired when frame has been detached from its parent.
 */
public class FrameDetachedEvent {

    /**
     * Id of the frame that has been detached.
     */
    private String frameId;
    /**
     * ('remove' | 'swap');
     */
    private String reason;
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }
}
