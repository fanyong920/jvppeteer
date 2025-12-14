package com.ruiyun.jvppeteer.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WindowState {
    @JsonProperty("normal")
    Normal,
    @JsonProperty("minimized")
    Minimized,
    @JsonProperty("maximized")
    Maximized,
    @JsonProperty("fullscreen")
    Fullscreen;
}
