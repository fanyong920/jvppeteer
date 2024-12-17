package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserPromptType {
    @JsonProperty("alert")
    Alert,
    @JsonProperty("beforeunload")
    Beforeunload,
    @JsonProperty("confirm")
    Confirm,
    @JsonProperty("prompt")
    Prompt


}
