package com.ruiyun.jvppeteer.cdp.entities;

public enum LengthUnit {
    CM("cm"),
    IN("in");

    private final String value;

    LengthUnit(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
