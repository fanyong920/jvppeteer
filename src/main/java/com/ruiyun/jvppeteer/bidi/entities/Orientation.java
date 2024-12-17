package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Orientation {
    @JsonProperty("portrait")
    Portrait,
    @JsonProperty("landscape")
    Landscape
}
