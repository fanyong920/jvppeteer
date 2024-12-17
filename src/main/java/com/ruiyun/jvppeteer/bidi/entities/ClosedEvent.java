package com.ruiyun.jvppeteer.bidi.entities;

public class ClosedEvent {
    private String reason;

    public ClosedEvent() {
    }

    public ClosedEvent(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ClosedEvent{" +
                "reason='" + reason + '\'' +
                '}';
    }
}
