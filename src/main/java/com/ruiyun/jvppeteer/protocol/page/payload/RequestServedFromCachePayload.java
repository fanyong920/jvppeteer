package com.ruiyun.jvppeteer.protocol.page.payload;

/**
 * Fired if request ended up loading from cache.
 */
public class RequestServedFromCachePayload {

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
