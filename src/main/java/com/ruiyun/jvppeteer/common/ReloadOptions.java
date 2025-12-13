package com.ruiyun.jvppeteer.common;

public class ReloadOptions extends WaitForOptions {
    private Boolean ignoreCache;

    public Boolean getIgnoreCache() {
        return ignoreCache;
    }

    public void setIgnoreCache(Boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
    }
}
