package com.ruiyun.jvppeteer.entities;

public enum DownloadBehavior {
    Deny("deny"),
    Allow("allow"),
    AllowAndName("allowAndName"),
    Default("default");

    DownloadBehavior(String behavior) {
        this.behavior = behavior.toLowerCase();
    }

    private final String behavior;

    public String getBehavior() {
        return behavior;
    }
}
