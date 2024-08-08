package com.ruiyun.jvppeteer.protocol.network;

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
