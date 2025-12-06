package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.cdp.entities.UserAgentMetadata;

public class UserAgentOptions {
    private String platform;
    private String userAgent;
    private UserAgentMetadata userAgentMetadata;

    public UserAgentOptions() {
    }

    public UserAgentOptions(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public UserAgentMetadata getUserAgentMetadata() {
        return userAgentMetadata;
    }

    public void setUserAgentMetadata(UserAgentMetadata userAgentMetadata) {
        this.userAgentMetadata = userAgentMetadata;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "UserAgentOptions{" +
                "platform='" + platform + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", userAgentMetadata=" + userAgentMetadata +
                '}';
    }
}
