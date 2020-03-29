package com.ruiyun.jvppeteer.events.browser.definition;

@FunctionalInterface
public interface EventHandler<T extends BrowserEvent> {

        void onEvent(T event);
}
