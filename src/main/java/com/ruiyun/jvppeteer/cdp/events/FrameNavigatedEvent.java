package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.FramePayload;

/**
 * Fired once navigation of the frame has completed. Frame is now associated with the new loader.
 */
public class FrameNavigatedEvent {
    /**
     * Frame object.
     */
    private FramePayload frame;
    /**
     * ('Navigation' | 'BackForwardCacheRestore');
     */
    private String type;
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public FramePayload getFrame() {
        return frame;
    }

    public void setFrame(FramePayload frame) {
        this.frame = frame;
    }
}
