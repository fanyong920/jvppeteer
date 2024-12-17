package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

public class ContinueRequestOverrides {
    private String url;
    private String method;
    private String postData;
    private List<HeaderEntry> headers;

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

    public List<HeaderEntry> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HeaderEntry> headers) {
        this.headers = headers;
    }
}
