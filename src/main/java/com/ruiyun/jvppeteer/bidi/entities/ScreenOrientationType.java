package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ScreenOrientationType {
    @JsonProperty
    PortraitPrimary("portrait-primary"),
    PortraitSecondary("portrait-secondary"),
    LandscapePrimary("landscape-primary"),
    LandscapeSecondary("landscape-secondary");
    private final String value;

    ScreenOrientationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
