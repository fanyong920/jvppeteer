package com.ruiyun.jvppeteer.options;

public enum LengthUnit {
    CM("cm"),
    IN("in");

    private String value;

    LengthUnit(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
