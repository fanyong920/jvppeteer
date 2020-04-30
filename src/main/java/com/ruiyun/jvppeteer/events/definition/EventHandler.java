package com.ruiyun.jvppeteer.events.definition;

@FunctionalInterface
public interface EventHandler<T> {

        void onEvent(T event);
}
