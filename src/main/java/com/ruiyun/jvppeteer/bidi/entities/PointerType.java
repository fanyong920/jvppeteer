package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PointerType {
    @JsonProperty("mouse")
    Mouse,
    @JsonProperty("pen")
    Pen,
    @JsonProperty("touch")
    Touch
}
