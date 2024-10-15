package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.entities.RequestPayload;
import com.ruiyun.jvppeteer.entities.HeaderEntry;

import java.util.List;

/**
 * Issued when the domain is enabled and the request URL matches the
 specified filter. The request is paused until the client responds
 with one of continueRequest, failRequest or fulfillRequest.
 The stage of the request can be determined by presence of responseErrorReason
 and responseStatusCode -- the request is at the response stage if either
 of these fields is present and in the request stage otherwise.
 */
public class RequestPausedEvent {

    /**
     * Each request the page makes will have a unique id.
     */
    private String requestId;
    /**
     * The details of the request.
     */
    private RequestPayload request;
    /**
     * The id of the frame that initiated the request.
     */
    private String frameId;
    /**
     * How the requested resource will be used.
     */
    private String resourceType;
    /**
     * Response error if intercepted at response stage.
     * "Failed"|"Aborted"|"TimedOut"|"AccessDenied"|"ConnectionClosed"|"ConnectionReset"|"ConnectionRefused"|"ConnectionAborted"|"ConnectionFailed"|"NameNotResolved"|"InternetDisconnected"|"AddressUnreachable"|"BlockedByClient"|"BlockedByResponse";
     */
    private String responseErrorReason;
    /**
     * Response code if intercepted at response stage.
     */
    private int responseStatusCode;
    /**
     * Response status text if intercepted at response stage.
     */
    private String responseStatusText;
    /**
     * Response headers if intercepted at the response stage.
     */
    private List<HeaderEntry> responseHeaders;
    /**
     * If the intercepted request had a corresponding Network.requestWillBeSent event fired for it,
     then this networkId will be the same as the requestId present in the requestWillBeSent event.
     */
    private String networkId;

    private String redirectedRequestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public RequestPayload getRequest() {
        return request;
    }

    public void setRequest(RequestPayload request) {
        this.request = request;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResponseErrorReason() {
        return responseErrorReason;
    }

    public void setResponseErrorReason(String responseErrorReason) {
        this.responseErrorReason = responseErrorReason;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(int responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public List<HeaderEntry> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(List<HeaderEntry> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getResponseStatusText() {
        return responseStatusText;
    }

    public void setResponseStatusText(String responseStatusText) {
        this.responseStatusText = responseStatusText;
    }

    public String getRedirectedRequestId() {
        return redirectedRequestId;
    }

    public void setRedirectedRequestId(String redirectedRequestId) {
        this.redirectedRequestId = redirectedRequestId;
    }
}
