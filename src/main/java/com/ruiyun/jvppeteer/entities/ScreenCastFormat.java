package com.ruiyun.jvppeteer.entities;

public enum ScreenCastFormat {
    WEBM("webm"),
    GIF("gif");
    private final String format;

    ScreenCastFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
