package com.ruiyun.jvppeteer.protocol.network;

import com.ruiyun.jvppeteer.types.page.Request;

/**
 * Fired when page is about to send HTTP request.
 */
public class RequestWillBeSentPayload {

    /**
     * Request identifier.
     */
    private String requestId;
    /**
     * Loader identifier. Empty string if the request is fetched from worker.
     */
    private String loaderId;
    /**
     * URL of the document this request is loaded for.
     */
    private String documentURL;
    /**
     * Request data.
     */
    private Request request;
    /**
     * Timestamp.
     */
    private long timestamp;
    /**
     * Timestamp.
     */
    private long wallTime;
    /**
     * Request initiator.
     */
    private Initiator initiator;
    /**
     * Redirect response data.
     */
    private ResponsePayload redirectResponse;
    /**
     * Type of this resource.
     * "Document"|"Stylesheet"|"Image"|"Media"|"Font"|"Script"|"TextTrack"|"XHR"|"Fetch"|"EventSource"|"WebSocket"|"Manifest"|"SignedExchange"|"Ping"|"CSPViolationReport"|"Other";
     */
    private String type;
    /**
     * Frame identifier.
     */
    private String frameId;
    /**
     * Whether the request is initiated by a user gesture. Defaults to false.
     */
    private boolean hasUserGesture;

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

    public String getDocumentURL() {
        return documentURL;
    }

    public void setDocumentURL(String documentURL) {
        this.documentURL = documentURL;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getWallTime() {
        return wallTime;
    }

    public void setWallTime(long wallTime) {
        this.wallTime = wallTime;
    }

    public Initiator getInitiator() {
        return initiator;
    }

    public void setInitiator(Initiator initiator) {
        this.initiator = initiator;
    }

    public ResponsePayload getRedirectResponse() {
        return redirectResponse;
    }

    public void setRedirectResponse(ResponsePayload redirectResponse) {
        this.redirectResponse = redirectResponse;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public boolean getHasUserGesture() {
        return hasUserGesture;
    }

    public void setHasUserGesture(boolean hasUserGesture) {
        this.hasUserGesture = hasUserGesture;
    }
}
