package com.ruiyun.jvppeteer.api.events;

import com.ruiyun.jvppeteer.cdp.entities.ConsoleMessage;

public enum WebWorkerEvent {
    /**
     * Emitted when the worker calls a console API.
     * {@link ConsoleMessage} 对应一个console事件
     */
    Console,
    /**
     * Emitted when the worker throws an exception.
     */
    Error,
}
