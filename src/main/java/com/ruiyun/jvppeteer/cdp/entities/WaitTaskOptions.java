package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.api.core.ElementHandle;

public class WaitTaskOptions {
    private String polling;
    private ElementHandle root;
    private int timeout;


    public WaitTaskOptions(String polling, ElementHandle root, int timeout) {
        this.polling = polling;
        this.timeout = timeout;
        this.root = root;
    }

    public String getPolling() {
        return polling;
    }

    public void setPolling(String polling) {
        this.polling = polling;
    }

    public ElementHandle getRoot() {
        return root;
    }

    public void setRoot(ElementHandle root) {
        this.root = root;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
