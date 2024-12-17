package com.ruiyun.jvppeteer.bidi.entities;

public enum InterceptPhase {
    BEFORE_REQUEST_SENT("beforeRequestSent"),
    RESPONSE_STARTED("responseStarted"),
    AUTH_REQUIRED("authRequired");

    private final String value;

    InterceptPhase(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
