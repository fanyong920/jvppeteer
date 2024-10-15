package com.ruiyun.jvppeteer.entities;

public enum ImageType {
    PNG,
    JPEG,
    WEBP,
    JPG;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
