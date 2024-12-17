package com.ruiyun.jvppeteer.api.events;

public enum FrameEvents {
    FrameNavigated("Frame.FrameNavigated"),
    FrameSwapped("Frame.FrameSwapped"),
    LifecycleEvent("Frame.LifecycleEvent"),
    FrameNavigatedWithinDocument("Frame.FrameNavigatedWithinDocument"),
    FrameDetached("Frame.FrameDetached"),
    FrameSwappedByActivation("Frame.FrameSwappedByActivation");
    private final String eventType;

    FrameEvents(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }
}
