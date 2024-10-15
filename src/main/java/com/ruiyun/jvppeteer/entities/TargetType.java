package com.ruiyun.jvppeteer.entities;

public enum TargetType {
    PAGE("page"),
    BACKGROUND_PAGE("background_page"),
    SERVICE_WORKER("service-worker"),
    SHARED_WORKER("shared-worker"),
    BROWSER("browser"),
    WEBVIEW("webview"),
    OTHER("other"),
    TAB("tab");
    private String type;
    TargetType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
