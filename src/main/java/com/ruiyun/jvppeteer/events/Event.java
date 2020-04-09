package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.events.browser.definition.BrowserListener;

/**
 * event
 */
public interface Event {

    Event addListener(String method, BrowserListener<?> listener, boolean isOnce);

    Event removeListener(String method, BrowserListener<?> listener);

    boolean emit(String type, Object parameter);

    default Event addListener(String method, BrowserListener<?> listener) {
        return this.addListener(method, listener, false);
    }

    default Event on(String method, BrowserListener<?> listener) {
        return this.addListener(method, listener);
    }

    default Event once(String method, BrowserListener<?> listener) {
        return this.addListener(method, listener, true);
    }

    default Event off(String method, BrowserListener<?> listener) {
        return this.removeListener(method, listener);
    }
}
