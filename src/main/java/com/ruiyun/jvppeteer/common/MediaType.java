package com.ruiyun.jvppeteer.common;

public enum MediaType {
    Screen("screen"),
    Print("print"),
    None("");

    private final String type;

    MediaType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
