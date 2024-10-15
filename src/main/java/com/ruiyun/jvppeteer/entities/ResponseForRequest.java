package com.ruiyun.jvppeteer.entities;

import java.util.List;

public class ResponseForRequest {
    private int status = 200;
    private List<HeaderEntry> headers;
    private String contentType;
    private String body;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<HeaderEntry>  getHeaders() {
        return headers;
    }

    public void setHeaders(List<HeaderEntry> headers) {
        this.headers = headers;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
