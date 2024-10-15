package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.core.ElementHandle;

public class WaitTaskOptions {
    private String polling;
    private ElementHandle root;
    private int timeout;
    //waitforfunction == true waitforselector = false
    private boolean predicateAcceptsContextElement;
    public WaitTaskOptions() {
    }

    public WaitTaskOptions(String polling, int timeout, ElementHandle root, boolean predicateAcceptsContextElement) {
        this.polling = polling;
        this.timeout = timeout;
        this.root = root;
        this.predicateAcceptsContextElement = predicateAcceptsContextElement;
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

    public boolean getPredicateAcceptsContextElement() {
        return predicateAcceptsContextElement;
    }

    public void setPredicateAcceptsContextElement(boolean predicateAcceptsContextElement) {
        this.predicateAcceptsContextElement = predicateAcceptsContextElement;
    }
}
