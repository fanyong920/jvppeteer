package com.ruiyun.jvppeteer.protocol.page.payload;

import com.ruiyun.jvppeteer.protocol.page.frame.Frame;

/**
 * Fired once navigation of the frame has completed. Frame is now associated with the new loader.
 */
public class FrameNavigatedPayload {
    /**
     * Frame object.
     */
    private Frame frame;

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }
}
