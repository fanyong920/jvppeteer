package com.ruiyun.jvppeteer.cdp.entities;

public enum ScreenCastFormat {
    WEBM("webm"),
    GIF("gif"),
    MP4("mp4");
    private final String format;

    ScreenCastFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
