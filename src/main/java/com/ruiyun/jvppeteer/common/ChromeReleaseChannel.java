package com.ruiyun.jvppeteer.common;

public enum ChromeReleaseChannel {
    STABLE("stable"),

    DEV("dev"),

    CANARY("canary"),

    BETA("beta"),
    /**
     * 专门给 CHROMIUM 使用的，CHROMIUM 没有 stable dev beta canary分类
     */
    LATEST("latest");
    private final String value;

    ChromeReleaseChannel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
