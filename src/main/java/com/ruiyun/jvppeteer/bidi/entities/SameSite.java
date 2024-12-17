package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SameSite {
    @JsonProperty("strict")
    Strict,
    @JsonProperty("lax")
    Lax,
    @JsonProperty("none")
    None
}
