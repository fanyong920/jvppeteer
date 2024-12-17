package com.ruiyun.jvppeteer.cdp.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CookiePriority {
    @JsonProperty("Low")
    Low,
    @JsonProperty("Medium")
    Medium,
    @JsonProperty("High")
    High,
}
