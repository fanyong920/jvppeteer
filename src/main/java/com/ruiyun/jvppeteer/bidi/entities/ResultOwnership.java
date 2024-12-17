package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ResultOwnership {
    @JsonProperty("root")
    Root,
    @JsonProperty("none")
    None
}
