package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.api.core.EventEmitter;
import java.util.function.Consumer;

public class DisposableStack<EventType> {
    private EventEmitter<EventType> emitter;
    private EventType type;
    private Consumer<?> consumer;

    public DisposableStack(EventEmitter<EventType> emitter, EventType type, Consumer<?> consumer) {
        this.emitter = emitter;
        this.type = type;
        this.consumer = consumer;
    }

    public EventEmitter<EventType> getEmitter() {
        return emitter;
    }

    public void setEmitter(EventEmitter<EventType> emitter) {
        this.emitter = emitter;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Consumer<?> getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer<?> consumer) {
        this.consumer = consumer;
    }
}
