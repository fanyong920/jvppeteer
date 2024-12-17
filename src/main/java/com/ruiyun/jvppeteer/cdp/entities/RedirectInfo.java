package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.cdp.events.RequestWillBeSentEvent;

public class RedirectInfo {
    private RequestWillBeSentEvent event;
    private String fetchRequestId;

    public RequestWillBeSentEvent getEvent() {
        return event;
    }

    public void setEvent(RequestWillBeSentEvent event) {
        this.event = event;
    }

    public String getFetchRequestId() {
        return fetchRequestId;
    }

    public void setFetchRequestId(String fetchRequestId) {
        this.fetchRequestId = fetchRequestId;
    }
}
