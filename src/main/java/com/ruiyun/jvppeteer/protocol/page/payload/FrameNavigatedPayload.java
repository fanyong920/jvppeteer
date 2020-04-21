package com.ruiyun.jvppeteer.protocol.page.payload;

/**
 * Fired once navigation of the frame has completed. Frame is now associated with the new loader.
 */
public class FrameNavigatedPayload {
    /**
     * Frame object.
     */
    private FramePayload frame;

    public FramePayload getFrame() {
        return frame;
    }

    public void setFrame(FramePayload frame) {
        this.frame = frame;
    }
}
