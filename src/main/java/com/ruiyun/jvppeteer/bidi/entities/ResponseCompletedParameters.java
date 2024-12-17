package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class ResponseCompletedParameters {
    private String context;
    private boolean isBlocked;
    private String navigation;
    private long redirectCount;
    private RequestData request;
    private long timestamp;
    private List<String> intercepts;
    private ResponseData response;

    public ResponseData getResponse() {
        return response;
    }

    public void setResponse(ResponseData response) {
        this.response = response;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public boolean getIsBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getNavigation() {
        return navigation;
    }

    public void setNavigation(String navigation) {
        this.navigation = navigation;
    }

    public RequestData getRequest() {
        return request;
    }

    public void setRequest(RequestData request) {
        this.request = request;
    }

    public long getRedirectCount() {
        return redirectCount;
    }

    public void setRedirectCount(long redirectCount) {
        this.redirectCount = redirectCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getIntercepts() {
        return intercepts;
    }

    public void setIntercepts(List<String> intercepts) {
        this.intercepts = intercepts;
    }
}
