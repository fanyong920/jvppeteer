package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.entities.ScreencastFrameMetadata;

public class ScreencastFrameEvent {
    private String data;
    private int sessionId;
    private ScreencastFrameMetadata metadata;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public ScreencastFrameMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ScreencastFrameMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "ScreencastFrameEvent{" +
                "data='" + data + '\'' +
                ", sessionId=" + sessionId +
                ", metadata=" + metadata +
                '}';
    }
}
