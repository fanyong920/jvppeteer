package com.ruiyun.jvppeteer.types.page.frame;

import com.ruiyun.jvppeteer.types.page.payload.FramePayload;

import java.util.List;

/**
 * Information about the Frame hierarchy.
 */
public class FrameTree {

    /**
     * Frame information for this tree item.
     */
    private FramePayload frame;
    /**
     * Child frames.
     */
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
