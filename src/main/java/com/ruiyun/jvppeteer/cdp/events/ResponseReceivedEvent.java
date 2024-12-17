package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.ResponsePayload;

import java.math.BigDecimal;

/**
 * Fired when HTTP response is available.
 */
public class ResponseReceivedEvent {

    /**
     * Request identifier.
     */
    private String requestId;
    /**
     * Loader identifier. Empty string if the request is fetched from worker.
     */
    private String loaderId;
    /**
     * Timestamp.
     */
    private BigDecimal timestamp;
    /**
     * Resource type.
     */
    private String type;
    /**
     * Response data.
     */
    private ResponsePayload response;
    /**
     * Frame identifier.
     */
    private String frameId;
    /**
     * Indicates whether requestWillBeSentExtraInfo and responseReceivedExtraInfo events will be
     * or were emitted for this request.
     */
    private boolean hasExtraInfo;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getLoaderId() {
        return loaderId;
    }

    public void setLoaderId(String loaderId) {
        this.loaderId = loaderId;
    }

    public BigDecimal getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigDecimal timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ResponsePayload getResponse() {
        return response;
    }

    public void setResponse(ResponsePayload response) {
        this.response = response;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public boolean getHasExtraInfo() {
        return hasExtraInfo;
    }

    public void setHasExtraInfo(boolean hasExtraInfo) {
        this.hasExtraInfo = hasExtraInfo;
    }
}
