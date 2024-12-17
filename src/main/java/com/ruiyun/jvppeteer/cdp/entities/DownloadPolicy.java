package com.ruiyun.jvppeteer.cdp.entities;

public enum DownloadPolicy {
    Deny("deny"),
    Allow("allow"),
    AllowAndName("allowAndName"),
    Default("default");

    DownloadPolicy(String behavior) {
        this.behavior = behavior.toLowerCase();
    }

    private final String behavior;

    public String getBehavior() {
        return behavior;
    }
}
