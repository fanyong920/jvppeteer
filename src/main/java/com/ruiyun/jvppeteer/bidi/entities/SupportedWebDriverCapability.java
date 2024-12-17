package com.ruiyun.jvppeteer.bidi.entities;

public class SupportedWebDriverCapability {
    private String browserName;
    private String browserVersion;
    private String platformName;
    private ProxyConfiguration proxy;

    public SupportedWebDriverCapability() {
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

    @Override
    public String toString() {
        return "SupportedWebDriverCapability{" +
                "browserName='" + browserName + '\'' +
                ", browserVersion='" + browserVersion + '\'' +
                ", platformName='" + platformName + '\'' +
                ", proxy=" + proxy +
                '}';
    }
}
