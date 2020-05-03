package com.ruiyun.jvppeteer.types.page.frame;

import com.ruiyun.jvppeteer.types.page.Response;
import com.ruiyun.jvppeteer.protocol.network.RequestWillBeSentPayload;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {



    private CDPSession client;

    private Frame frame;

    private String interceptionId;

    private boolean allowInterception;

    private RequestWillBeSentPayload event;

    private List<Request> redirectChain;

    private Response response;

    private String requestId;

    private boolean isNavigationRequest;

    private boolean interceptionHandled;

    private String failureText;

    private String url;

    private String  resourceType;

    private String method;

    private String postData;

    private Map<String,String> headers;

    private boolean fromMemoryCache;

    public Request() {
        super();
    }

    public Request(CDPSession client, Frame frame, String interceptionId, boolean allowInterception, RequestWillBeSentPayload event, List<Request> redirectChain) {
        super();
        this.client = client;
        this.requestId = event.getRequestId();
        if(event.getRequestId() != null){
            if(event.getRequestId().equals(event.getLoaderId()))
            this.isNavigationRequest = true;
        }else{
            if(event.getLoaderId() == null)
                this.isNavigationRequest = true;
        }
        this.interceptionId = interceptionId;
        this.allowInterception = allowInterception;
        this.interceptionHandled = false;
        this.response = null;
        this.failureText = null;

        this.url = event.getRequest().getUrl();
        this.resourceType = event.getType().toLowerCase();
        this.method = event.getRequest().getMethod();
        this.postData = event.getRequest().getPostData();
        this.headers = new HashMap<>();
        this.frame = frame;
        this.event = event;
        this.redirectChain = redirectChain;
        for(Map.Entry<String,String> entry : event.getRequest().headers.entrySet()){
            this.headers.put(entry.getKey(),entry.getValue());
            this.fromMemoryCache = false;
        }
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

    public Response response() {
        return  this.response;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean getIsNavigationRequest() {
        return isNavigationRequest;
    }

    public void setNavigationRequest(boolean navigationRequest) {
        isNavigationRequest = navigationRequest;
    }

    public boolean getIsInterceptionHandled() {
        return interceptionHandled;
    }

    public void setInterceptionHandled(boolean interceptionHandled) {
        this.interceptionHandled = interceptionHandled;
    }

    public String getFailureText() {
        return failureText;
    }

    public void setFailureText(String failureText) {
        this.failureText = failureText;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public boolean getIsFromMemoryCache() {
        return fromMemoryCache;
    }

    public void setFromMemoryCache(boolean fromMemoryCache) {
        this.fromMemoryCache = fromMemoryCache;
    }
}
