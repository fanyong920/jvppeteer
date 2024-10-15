package com.ruiyun.jvppeteer.events;

public class FrameStoppedLoadingEvent {

    /**
     * id of the frame that has stopped loading.
     */
    private String frameId;

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

}
