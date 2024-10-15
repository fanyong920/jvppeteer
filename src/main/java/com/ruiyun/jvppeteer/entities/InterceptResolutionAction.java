package com.ruiyun.jvppeteer.entities;

public enum InterceptResolutionAction {
    ABORT("abort"),
    RESPOND("respond"),
    CONTINUE("continue"),
    DISABLED("disabled"),
    NONE("none"),
    ALREADY_HANDLED("already-handled");

    private final String action;

    InterceptResolutionAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
