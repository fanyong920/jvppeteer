package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.cdp.entities.FramePayload;

import java.util.List;

public class FrameTreeEvent {
    private FramePayload frame;
    private List<FrameTreeEvent> childFrames;

    public FramePayload getFrame() {
        return frame;
    }

    public void setFrame(FramePayload frame) {
        this.frame = frame;
    }

    public List<FrameTreeEvent> getChildFrames() {
        return childFrames;
    }

    public void setChildFrames(List<FrameTreeEvent> childFrames) {
        this.childFrames = childFrames;
    }


}
