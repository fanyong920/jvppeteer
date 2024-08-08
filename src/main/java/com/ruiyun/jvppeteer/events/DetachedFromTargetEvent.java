package com.ruiyun.jvppeteer.events;

public class DetachedFromTargetEvent {
    private String sessionId;
    private String targetId;
    public DetachedFromTargetEvent() {
    }
    public DetachedFromTargetEvent(String sessionId,String targetId) {
        this.sessionId = sessionId;
        this.targetId = targetId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    public String getTargetId() {
        return targetId;
    }

    @Override
    public String toString() {
        return "DetachedFromTargetEvent{" +
                "sessionId='" + sessionId + '\'' +
                ", targetId='" + targetId + '\'' +
                '}';
    }
}

