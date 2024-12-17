package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class ProxyConfiguration {
    private String proxyType;
    private String ftpProxy;
    private String httpProxy;
    private String sslProxy;
    private String socksProxy;
    private String socksVersion;
    private List<String> noProxy;
    private String proxyAutoconfigUrl;

    public ProxyConfiguration() {
    }

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getFtpProxy() {
        return ftpProxy;
    }

    public void setFtpProxy(String ftpProxy) {
        this.ftpProxy = ftpProxy;
    }

    public String getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(String httpProxy) {
        this.httpProxy = httpProxy;
    }

    public String getSslProxy() {
        return sslProxy;
    }

    public void setSslProxy(String sslProxy) {
        this.sslProxy = sslProxy;
    }

    public String getSocksProxy() {
        return socksProxy;
    }

    public void setSocksProxy(String socksProxy) {
        this.socksProxy = socksProxy;
    }

    public String getSocksVersion() {
        return socksVersion;
    }

    public void setSocksVersion(String socksVersion) {
        this.socksVersion = socksVersion;
    }

    public List<String> getNoProxy() {
        return noProxy;
    }

    public void setNoProxy(List<String> noProxy) {
        this.noProxy = noProxy;
    }

    public String getProxyAutoconfigUrl() {
        return proxyAutoconfigUrl;
    }

    public void setProxyAutoconfigUrl(String proxyAutoconfigUrl) {
        this.proxyAutoconfigUrl = proxyAutoconfigUrl;
    }

    @Override
    public String toString() {
        return "ProxyConfiguration{" +
                "proxyType='" + proxyType + '\'' +
                ", ftpProxy='" + ftpProxy + '\'' +
                ", httpProxy='" + httpProxy + '\'' +
                ", sslProxy='" + sslProxy + '\'' +
                ", socksProxy='" + socksProxy + '\'' +
                ", socksVersion='" + socksVersion + '\'' +
                ", noProxy=" + noProxy +
                ", proxyAutoconfigUrl='" + proxyAutoconfigUrl + '\'' +
                '}';
    }
}
