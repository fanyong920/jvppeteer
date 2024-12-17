package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.bidi.core.ReadinessState;

public class ReloadParameters {
    private String context;
    private Boolean ignoreCache;
    private ReadinessState wait;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Boolean getIgnoreCache() {
        return ignoreCache;
    }

    public void setIgnoreCache(Boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
    }

    public ReadinessState getWait() {
        return wait;
    }

    public void setWait(ReadinessState wait) {
        this.wait = wait;
    }
}
