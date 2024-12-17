package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SourceActionsType {
    @JsonProperty("none")
    NONE,
    @JsonProperty("key")
    Key,
    @JsonProperty("pointer")
    Pointer,
    @JsonProperty("wheel")
    Wheel,
    @JsonProperty("pause")
    Pause,
    @JsonProperty("keyDown")
    KeyDown,
    @JsonProperty("keyUp")
    KeyUp,
    @JsonProperty("pointerUp")
    PointerUp,
    @JsonProperty("pointerDown")
    PointerDown,
    @JsonProperty("pointerMove")
    PointerMove,
    @JsonProperty("scroll")
    Scroll,
}
