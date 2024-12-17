package com.ruiyun.jvppeteer.cdp.events;

/**
 * Fired if request ended up loading from cache.
 */
public class RequestServedFromCacheEvent {

    /**
     * Request identifier.
     */
    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
