package com.ruiyun.jvppeteer.entities;

public enum ResourceType {
    Document("Document"),
    Stylesheet("Stylesheet"),
    Image("Image"),
    Media("Media"),
    Font("Font"),
    Script("Script"),
    TextTrack("TextTrack"),
    XHR("XHR"),
    Fetch("Fetch"),
    EventSource("EventSource"),
    WebSocket("WebSocket"),
    Manifest("Manifest"),
    SignedExchange("SignedExchange"),
    Ping("Ping"),
    CSPViolationReport("CSPViolationReport"),
    Preflight("Preflight"),
    Other("Other");

    private String type;

    ResourceType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
