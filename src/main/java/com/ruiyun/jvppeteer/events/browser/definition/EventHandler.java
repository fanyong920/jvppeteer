package com.ruiyun.jvppeteer.events.browser.definition;

@FunctionalInterface
public interface EventHandler<T> {

        void onEvent(T event);
}
