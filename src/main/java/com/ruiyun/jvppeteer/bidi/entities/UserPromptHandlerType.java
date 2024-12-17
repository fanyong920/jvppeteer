package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserPromptHandlerType {
    @JsonProperty("accept")
    Accept("accept"),
    @JsonProperty("dismiss")
    Dismiss("dismiss"),
    @JsonProperty("ignore")
    Ignore("ignore");
    private final String type;
    UserPromptHandlerType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
}
