package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Origin {
    @JsonProperty("viewport")
    Viewport,
    @JsonProperty("document")
    Document
}
