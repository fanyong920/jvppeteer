package com.ruiyun.jvppeteer.protocol.page.frame;

import com.ruiyun.jvppeteer.protocol.page.payload.RequestWillBeSentPayload;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.List;

public class Request {

    private CDPSession client;

    private Frame frame;

    private String interceptionId;

    private boolean allowInterception;

    private RequestWillBeSentPayload event;

    private List<Request> redirectChain;

    public Request() {
        super();
    }

    public Request(CDPSession client, Frame frame, String interceptionId, boolean allowInterception, RequestWillBeSentPayload event, List<Request> redirectChain) {
        super();
        this.client = client;
        this.frame = frame;
        this.interceptionId = interceptionId;
        this.allowInterception = allowInterception;
        this.event = event;
        this.redirectChain = redirectChain;
    }

    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public String getInterceptionId() {
        return interceptionId;
    }

    public void setInterceptionId(String interceptionId) {
        this.interceptionId = interceptionId;
    }

    public boolean isAllowInterception() {
        return allowInterception;
    }

    public void setAllowInterception(boolean allowInterception) {
        this.allowInterception = allowInterception;
    }

    public RequestWillBeSentPayload getEvent() {
        return event;
    }

    public void setEvent(RequestWillBeSentPayload event) {
        this.event = event;
    }

    public List<Request> getRedirectChain() {
        return redirectChain;
    }

    public void setRedirectChain(List<Request> redirectChain) {
        this.redirectChain = redirectChain;
    }
}
