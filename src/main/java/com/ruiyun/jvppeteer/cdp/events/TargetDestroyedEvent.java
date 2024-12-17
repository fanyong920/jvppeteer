package com.ruiyun.jvppeteer.cdp.events;

public class TargetDestroyedEvent {

    private String targetId;

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}
