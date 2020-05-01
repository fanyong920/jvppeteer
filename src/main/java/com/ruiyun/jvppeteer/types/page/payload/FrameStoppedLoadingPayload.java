package com.ruiyun.jvppeteer.types.page.payload;

/**
 * Fired when frame has stopped loading.
 */
public class FrameStoppedLoadingPayload {

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
