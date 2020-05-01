package com.ruiyun.jvppeteer.types.page.payload;

/**
 * Fired when HTTP request has finished loading.
 */
public class LoadingFinishedPayload {

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
    /**
     * Set when 1) response was blocked by Cross-Origin Read Blocking and also
     2) this needs to be reported to the DevTools console.
     */
    private boolean shouldReportCorbBlocking;

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

    public boolean getIsShouldReportCorbBlocking() {
        return shouldReportCorbBlocking;
    }

    public void setShouldReportCorbBlocking(boolean shouldReportCorbBlocking) {
        this.shouldReportCorbBlocking = shouldReportCorbBlocking;
    }
}
