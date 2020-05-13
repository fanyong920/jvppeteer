package com.ruiyun.jvppeteer.events;

/**
 * 事件处理器
 * @param <T>
 */
@FunctionalInterface
public interface EventHandler<T> {

    void onEvent(T event);
}
