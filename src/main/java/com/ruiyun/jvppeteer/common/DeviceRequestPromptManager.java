package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.events.DeviceRequestPromptedEvent;
import com.ruiyun.jvppeteer.transport.CDPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DeviceRequestPromptManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRequestPromptManager.class);
    private CDPSession client;
    private final TimeoutSettings timeoutSettings;
    private final Set<AwaitableResult<DeviceRequestPrompt>> deviceRequestPromps = new HashSet<>();

    public DeviceRequestPromptManager(CDPSession client, TimeoutSettings timeoutSettings) {
        this.client = client;
        this.timeoutSettings = timeoutSettings;
        this.client.on(CDPSession.CDPSessionEvent.DeviceAccess_deviceRequestPrompted, (event) -> this.onDeviceRequestPrompted((DeviceRequestPromptedEvent) event));
        this.client.on(CDPSession.CDPSessionEvent.Target_detachedFromTarget, (ignore) -> this.client = null);
    }

    private void onDeviceRequestPrompted(DeviceRequestPromptedEvent event) {
        if (this.deviceRequestPromps.isEmpty()) return;
        Objects.requireNonNull(this.client, "Session is detached");
        DeviceRequestPrompt devicePrompt = new DeviceRequestPrompt(this.client, this.timeoutSettings, event);
        for (AwaitableResult<DeviceRequestPrompt> subject : this.deviceRequestPromps) {
            subject.onSuccess(devicePrompt);
        }
        this.deviceRequestPromps.clear();
    }

    public DeviceRequestPrompt waitForDevicePrompt(int timeout) {
        Objects.requireNonNull(this.client, "Cannot wait for device prompt through detached session!");
        boolean needsEnable = this.deviceRequestPromps.isEmpty();
        AwaitableResult<DeviceRequestPrompt> enableSubject = AwaitableResult.create();
        try {
            this.deviceRequestPromps.add(enableSubject);
            if (needsEnable) {
                this.client.send("DeviceAccess.enable");
            }
            return enableSubject.waitingGetResult(timeout, TimeUnit.MILLISECONDS);
        } finally {
            this.deviceRequestPromps.remove(enableSubject);
        }
    }
}
