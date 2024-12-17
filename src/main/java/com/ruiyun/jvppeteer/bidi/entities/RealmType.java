package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RealmType {
    @JsonProperty("window")
    Window("window"),
    @JsonProperty("dedicated-worker")
    DedicatedWorker("dedicated-worker"),
    @JsonProperty("shared-worker")
    SharedWorker("shared-worker"),
    @JsonProperty("service-worker")
    ServiceWorker("service-worker"),
    @JsonProperty("worker")
    Worker("worker"),
    @JsonProperty("paint-worklet")
    PaintWorklet("paint-worklet"),
    @JsonProperty("audio-worklet")
    AudioWorklet("audio-worklet"),
    @JsonProperty("worklet")
    Worklet("worklet");

    private final String type;

    RealmType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
