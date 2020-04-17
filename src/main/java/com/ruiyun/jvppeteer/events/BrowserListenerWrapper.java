package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.events.browser.impl.DefaultBrowserListener;

public class BrowserListenerWrapper<T> {

    private EventEmitter emitter;

    private String eventName;

    private DefaultBrowserListener<T> handler;

    public BrowserListenerWrapper() {
    }

    public BrowserListenerWrapper(EventEmitter emitter, String eventName, DefaultBrowserListener<T> handler) {
        this.emitter = emitter;
        this.eventName = eventName;
        this.handler = handler;
    }

    public EventEmitter getEmitter() {
        return emitter;
    }

    public void setEmitter(EventEmitter emitter) {
        this.emitter = emitter;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public DefaultBrowserListener<T> getHandler() {
        return handler;
    }

    public void setHandler(DefaultBrowserListener<T> handler) {
        this.handler = handler;
    }
}
