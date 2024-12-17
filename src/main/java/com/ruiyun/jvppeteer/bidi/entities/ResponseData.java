package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruiyun.jvppeteer.cdp.entities.AuthChallenge;
import com.ruiyun.jvppeteer.cdp.entities.SecurityDetails;
import java.util.List;

public class ResponseData {
    private String url;
    private String protocol;
    private int status;
    private String statusText;
    private boolean fromCache;
    private List<Header> headers;
    private String mimeType;
    private long bytesReceived;
    private Long headersSize;
    private Long bodySize;
    private ResponseContent content;
    private List<AuthChallenge> authChallenges;
    @JsonProperty("goog:securityDetails")
    private SecurityDetails securityDetails;

    public SecurityDetails getSecurityDetails() {
        return securityDetails;
    }

    public void setSecurityDetails(SecurityDetails securityDetails) {
        this.securityDetails = securityDetails;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<AuthChallenge> getAuthChallenges() {
        return authChallenges;
    }

    public void setAuthChallenges(List<AuthChallenge> authChallenges) {
        this.authChallenges = authChallenges;
    }

    public ResponseContent getContent() {
        return content;
    }

    public void setContent(ResponseContent content) {
        this.content = content;
    }

    public Long getBodySize() {
        return bodySize;
    }

    public void setBodySize(Long bodySize) {
        this.bodySize = bodySize;
    }

    public Long getHeadersSize() {
        return headersSize;
    }

    public void setHeadersSize(Long headersSize) {
        this.headersSize = headersSize;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public boolean getFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "url='" + url + '\'' +
                ", protocol='" + protocol + '\'' +
                ", status=" + status +
                ", statusText='" + statusText + '\'' +
                ", fromCache=" + fromCache +
                ", headers=" + headers +
                ", mimeType='" + mimeType + '\'' +
                ", bytesReceived=" + bytesReceived +
                ", headersSize=" + headersSize +
                ", bodySize=" + bodySize +
                ", content=" + content +
                ", authChallenges=" + authChallenges +
                '}';
    }
}
