package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.common.ProxyConfiguration;

public class CapabilityRequest {
    private boolean acceptInsecureCerts;
    private String browserName;
    private String browserVersion;
    private String platformName;
    private ProxyConfiguration proxy;
    private UserPromptHandler unhandledPromptBehavior;
    private  boolean webSocketUrl;
    public boolean getAcceptInsecureCerts() {
        return acceptInsecureCerts;
    }

    public void setAcceptInsecureCerts(boolean acceptInsecureCerts) {
        this.acceptInsecureCerts = acceptInsecureCerts;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public ProxyConfiguration getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfiguration proxy) {
        this.proxy = proxy;
    }

    public UserPromptHandler getUnhandledPromptBehavior() {
        return unhandledPromptBehavior;
    }

    public void setUnhandledPromptBehavior(UserPromptHandler unhandledPromptBehavior) {
        this.unhandledPromptBehavior = unhandledPromptBehavior;
    }

    public boolean getWebSocketUrl() {
        return webSocketUrl;
    }

    public void setWebSocketUrl(boolean webSocketUrl) {
        this.webSocketUrl = webSocketUrl;
    }
}
