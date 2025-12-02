package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.RequestDevicePromptUpdatedParameters;
import com.ruiyun.jvppeteer.common.AwaitableResult;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BidiDeviceRequestPromptManager {
    private final Session session;
    private final String contextId;
    private boolean enabled = false;

    public BidiDeviceRequestPromptManager(Session session, String contextId) {
        this.session = session;
        this.contextId = contextId;
    }

    public void enableIfNeeded() {
        if (!this.enabled) {
            this.enabled = true;
            this.session.subscribe(Collections.singletonList("bluetooth.requestDevicePromptUpdated"), Collections.singletonList(this.contextId));
        }
    }

    public DeviceRequestPrompt waitForDevicePrompt(Integer timeout) {
        AwaitableResult<BidiDeviceRequestPrompt> waitableResult = AwaitableResult.create();
        this.session.on(ConnectionEvents.bluetooth_requestDevicePromptUpdated, onRequestDevicePromptUpdated(waitableResult));
        this.enableIfNeeded();
        boolean waiting = waitableResult.waiting(timeout, TimeUnit.MILLISECONDS);
        if (!waiting) {
            throw new TimeoutException("Waiting for `DeviceRequestPromptDevice` failed: " + timeout + "ms exceeded");
        }
        return waitableResult.get();
    }

    private Consumer<RequestDevicePromptUpdatedParameters> onRequestDevicePromptUpdated(AwaitableResult<BidiDeviceRequestPrompt> waitableResult) {
        return pramas -> {
            if (Objects.equals(pramas.getId(), this.contextId)) {
                waitableResult.complete(new BidiDeviceRequestPrompt(this.contextId, pramas.getPrompt(), this.session, pramas.getDevices()));
            }
            this.session.off(ConnectionEvents.bluetooth_requestDevicePromptUpdated, onRequestDevicePromptUpdated(waitableResult));
        };
    }
}
