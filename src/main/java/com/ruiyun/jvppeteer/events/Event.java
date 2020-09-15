package com.ruiyun.jvppeteer.events;

/**
 * event
 */
public interface Event {

    Event addListener(String method, BrowserListener<?> listener, boolean isOnce);

    Event removeListener(String method, BrowserListener<?> listener);

    void emit(String type, Object parameter);

    default Event addListener(String method, BrowserListener<?> listener) {
        return this.addListener(method, listener, false);
    }
}
