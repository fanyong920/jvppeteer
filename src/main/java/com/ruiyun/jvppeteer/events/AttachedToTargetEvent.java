package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.core.page.TargetInfo;

public class AttachedToTargetEvent {
    private String sessionId;
    private TargetInfo targetInfo;
    private boolean waitingForDebugger;

    public AttachedToTargetEvent() {
    }

    public AttachedToTargetEvent(String sessionId, TargetInfo targetInfo, boolean waitingForDebugger) {
        this.sessionId = sessionId;
        this.targetInfo = targetInfo;
        this.waitingForDebugger = waitingForDebugger;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public TargetInfo getTargetInfo() {
        return targetInfo;
    }
    public void setTargetInfo(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
    }
    public boolean getWaitingForDebugger() {
        return waitingForDebugger;
    }
    public void setWaitingForDebugger(boolean waitingForDebugger) {
        this.waitingForDebugger = waitingForDebugger;
    }

    @Override
    public String toString() {
        return "AttachedToTargetEvent{" +
                "sessionId='" + sessionId + '\'' +
                ", targetInfo=" + targetInfo +
                ", waitingForDebugger=" + waitingForDebugger +
                '}';
    }
}
