package com.ruiyun.jvppeteer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DialogType {
    @JsonProperty("alert")
    Alert("alert"),
    @JsonProperty("beforeunload")
    BeforeUnload("beforeunload"),
    @JsonProperty("confirm")
    Confirm("confirm"),
    @JsonProperty("prompt")
    Prompt("prompt");

    private String type;

    DialogType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
