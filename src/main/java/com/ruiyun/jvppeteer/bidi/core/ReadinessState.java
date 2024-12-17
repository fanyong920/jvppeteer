package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ReadinessState {
    @JsonProperty("none")
    None,
    @JsonProperty("interactive")
    Interactive,
    @JsonProperty("complete")
    Complete
}
