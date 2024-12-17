package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.cdp.events.LoadingFailedEvent;
import com.ruiyun.jvppeteer.cdp.events.LoadingFinishedEvent;
import com.ruiyun.jvppeteer.cdp.events.ResponseReceivedEvent;

public class QueuedEventGroup {
    private ResponseReceivedEvent responseReceivedEvent;
    private LoadingFinishedEvent loadingFinishedEvent;
    private LoadingFailedEvent loadingFailedEvent;

    public ResponseReceivedEvent getResponseReceivedEvent() {
        return responseReceivedEvent;
    }

    public void setResponseReceivedEvent(ResponseReceivedEvent responseReceivedEvent) {
        this.responseReceivedEvent = responseReceivedEvent;
    }

    public LoadingFinishedEvent getLoadingFinishedEvent() {
        return loadingFinishedEvent;
    }

    public void setLoadingFinishedEvent(LoadingFinishedEvent loadingFinishedEvent) {
        this.loadingFinishedEvent = loadingFinishedEvent;
    }

    public LoadingFailedEvent getLoadingFailedEvent() {
        return loadingFailedEvent;
    }

    public void setLoadingFailedEvent(LoadingFailedEvent loadingFailedEvent) {
        this.loadingFailedEvent = loadingFailedEvent;
    }
}
