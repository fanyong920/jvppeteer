package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScreenOrientationNatural {
    Portrait("portrait"),
    Landscape("landscape");
    private final String value;

    ScreenOrientationNatural(String value) {
        this.value = value;
    }
    @JsonValue
    public String getValue() {
        return value;
    }
}
