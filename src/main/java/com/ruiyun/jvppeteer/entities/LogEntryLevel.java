package com.ruiyun.jvppeteer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum LogEntryLevel {
    @JsonProperty("verbose")
    VERBOSE,
    @JsonProperty("info")
    INFO,
    @JsonProperty("warning")
    WARNING,
    @JsonProperty("error")
    ERROR
}
