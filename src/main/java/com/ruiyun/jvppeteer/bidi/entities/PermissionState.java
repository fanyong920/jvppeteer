package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PermissionState {
    @JsonProperty("granted")
    Granted,
    @JsonProperty("denied")
    Denied,
    @JsonProperty("prompt")
    Prompt
}
