package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.entities.AuthChallenge;
import com.ruiyun.jvppeteer.entities.RequestPayload;

/**
 * Issued when the domain is enabled with handleAuthRequests set to true.
 The request is paused until client responds with continueWithAuth.
 */
public class AuthRequiredEvent {

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
     * Details of the Authorization Challenge encountered.
     If this is set, client should respond with continueRequest that
     contains AuthChallengeResponse.
     */
    private AuthChallenge pauthChallenge;

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

    public AuthChallenge getPauthChallenge() {
        return pauthChallenge;
    }

    public void setPauthChallenge(AuthChallenge pauthChallenge) {
        this.pauthChallenge = pauthChallenge;
    }
}
