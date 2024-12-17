package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RequestData {
    private String request;
    private String url;
    private String method;
    private List<Header> headers;
    private List<Cookie> cookies;
    private long headersSize;
    private long bodySize;
    private FetchTimingInfo timings;
    @JsonProperty("goog:resourceType")
    private String resourceType;
    @JsonProperty("goog:postData")
    private String postData;
    @JsonProperty("goog:hasPostData")
    private boolean hasPostData;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public FetchTimingInfo getTimings() {
        return timings;
    }

    public void setTimings(FetchTimingInfo timings) {
        this.timings = timings;
    }

    public long getBodySize() {
        return bodySize;
    }

    public void setBodySize(long bodySize) {
        this.bodySize = bodySize;
    }

    public long getHeadersSize() {
        return headersSize;
    }

    public void setHeadersSize(long headersSize) {
        this.headersSize = headersSize;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public boolean getHasPostData() {
        return hasPostData;
    }

    public void setHasPostData(boolean hasPostData) {
        this.hasPostData = hasPostData;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    @Override
    public String toString() {
        return "RequestData{" +
                "request='" + request + '\'' +
                ", url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", headers=" + headers +
                ", cookies=" + cookies +
                ", headersSize=" + headersSize +
                ", bodySize=" + bodySize +
                ", timings=" + timings +
                ", resourceType='" + resourceType + '\'' +
                ", postData='" + postData + '\'' +
                ", hasPostData=" + hasPostData +
                '}';
    }
}
