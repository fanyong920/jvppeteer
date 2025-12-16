package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.common.ProxyConfiguration;

public class Capabilities {
    private boolean acceptInsecureCerts;
    private String browserName;
    private String browserVersion;
    private String platformName;
    private boolean setWindowRect;
    private String userAgent;
    private ProxyConfiguration proxy;
    private UserPromptHandler unhandledPromptBehavior;
    private String webSocketUrl;

    public boolean getAcceptInsecureCerts() {
        return acceptInsecureCerts;
    }

    public void setAcceptInsecureCerts(boolean acceptInsecureCerts) {
        this.acceptInsecureCerts = acceptInsecureCerts;
    }

    public String getWebSocketUrl() {
        return webSocketUrl;
    }

    public void setWebSocketUrl(String webSocketUrl) {
        this.webSocketUrl = webSocketUrl;
    }

    public UserPromptHandler getUnhandledPromptBehavior() {
        return unhandledPromptBehavior;
    }

    public void setUnhandledPromptBehavior(UserPromptHandler unhandledPromptBehavior) {
        this.unhandledPromptBehavior = unhandledPromptBehavior;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean getSetWindowRect() {
        return setWindowRect;
    }

    public void setSetWindowRect(boolean setWindowRect) {
        this.setWindowRect = setWindowRect;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public ProxyConfiguration getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfiguration proxy) {
        this.proxy = proxy;
    }
}
