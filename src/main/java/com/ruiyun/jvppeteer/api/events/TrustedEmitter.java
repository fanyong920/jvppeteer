package com.ruiyun.jvppeteer.api.events;

import com.ruiyun.jvppeteer.api.core.EventEmitter;
import java.util.ArrayList;
import java.util.List;

public class TrustedEmitter<EventType> extends EventEmitter<EventType> {
    private final List<EventEmitter<EventType>> emitters = new ArrayList<>();

    @Override
    public <T> void emit(EventType event, T param) {
        for (EventEmitter<EventType> emitter : this.emitters) {
            try {
                emitter.emit(event, param);
            } catch (Exception e) {
                LOGGER.error("jvppeteer error", e);
            }
        }
        super.emit(event, param);
    }

    public void pipeTo(EventEmitter<EventType> emitter) {
        this.emitters.add(emitter);
    }
}
