package com.ruiyun.jvppeteer.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PollingOptions {
    @JsonProperty("raf")
    Raf,
    @JsonProperty("mutation")
    Mutation

}
