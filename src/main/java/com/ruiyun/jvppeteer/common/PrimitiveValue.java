package com.ruiyun.jvppeteer.common;

public enum PrimitiveValue {
    Undefined("undefined"),
    Null("null");
    private final String value;

    PrimitiveValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
