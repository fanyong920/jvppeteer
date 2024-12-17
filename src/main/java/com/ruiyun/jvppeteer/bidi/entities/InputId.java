package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum InputId {
    @JsonProperty("__puppeteer_mouse")
    Mouse("__puppeteer_mouse"),
    @JsonProperty("__puppeteer_keyboard")
    Keyboard("__puppeteer_keyboard"),
    @JsonProperty("__puppeteer_wheel")
    Wheel("__puppeteer_wheel"),
    @JsonProperty("__puppeteer_finger")
    Finger("__puppeteer_finger");
    private final String id;

    InputId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
