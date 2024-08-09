package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.protocol.page.FramePayload;

import java.util.List;

public class FrameTree {
    private FramePayload frame;
    private List<FrameTree> childFrames;

    public FramePayload getFrame() {
        return frame;
    }

    public void setFrame(FramePayload frame) {
        this.frame = frame;
    }

    public List<FrameTree> getChildFrames() {
        return childFrames;
    }

    public void setChildFrames(List<FrameTree> childFrames) {
        this.childFrames = childFrames;
    }
}
