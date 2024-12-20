package com.ruiyun.jvppeteer.cdp.entities;

public class GetVersionResponse {
    private String protocolVersion;
    private String product;
    private String revision;
    private String userAgent;
    private String jsVersion;
    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }
    public String getRevision() {
        return revision;
    }
    public void setRevision(String revision) {
        this.revision = revision;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getJsVersion() {
        return jsVersion;
    }
    public void setJsVersion(String jsVersion) {
        this.jsVersion = jsVersion;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
}
