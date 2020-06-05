package com.ruiyun.jvppeteer.events;

/**
 * 自定义事件处理器的接口，想要监听浏览器某个事件然后处理，必须实现这个处理器
 *
 */
@FunctionalInterface
public interface EventHandler<T> {

    void onEvent(T event);
}
