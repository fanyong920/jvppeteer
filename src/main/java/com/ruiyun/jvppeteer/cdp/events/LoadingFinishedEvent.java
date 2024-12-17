package com.ruiyun.jvppeteer.cdp.events;

/**
 * Fired when HTTP request has finished loading.
 */
public class LoadingFinishedEvent {

    /**
     * Request identifier.
     */
    private String requestId;
    /**
     * Timestamp.
     */
    private long timestamp;
    /**
     * Total number of bytes received for this request.
     */
    private int encodedDataLength;


    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getEncodedDataLength() {
        return encodedDataLength;
    }

    public void setEncodedDataLength(int encodedDataLength) {
        this.encodedDataLength = encodedDataLength;
    }

}
