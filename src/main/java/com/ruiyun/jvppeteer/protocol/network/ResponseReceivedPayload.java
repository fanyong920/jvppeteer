package com.ruiyun.jvppeteer.protocol.network;

/**
 * Fired when HTTP response is available.
 */
public class ResponseReceivedPayload {

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
    private long timestamp;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
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
}
