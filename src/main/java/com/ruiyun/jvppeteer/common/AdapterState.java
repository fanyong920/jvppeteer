package com.ruiyun.jvppeteer.common;

public enum AdapterState {
    Absent("absent"),
    PoweredOff("powered-off"),
    poweredOn("powered-on");

    private final String state;

    AdapterState(String state) {
        this.state = state;
    }
    public String getState() {
        return state;
    }
}
