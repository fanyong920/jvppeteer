package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.cdp.core.CdpDeviceRequestPrompt;
import com.ruiyun.jvppeteer.cdp.events.DeviceRequestPromptedEvent;
import com.ruiyun.jvppeteer.exception.TimeoutException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceRequestPromptManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRequestPromptManager.class);
    private CDPSession client;
    private final TimeoutSettings timeoutSettings;
    private final Set<AwaitableResult<CdpDeviceRequestPrompt>> deviceRequestPromps = new HashSet<>();

    public DeviceRequestPromptManager(CDPSession client, TimeoutSettings timeoutSettings) {
        this.client = client;
        this.timeoutSettings = timeoutSettings;
        this.client.on(ConnectionEvents.DeviceAccess_deviceRequestPrompted, (event) -> this.onDeviceRequestPrompted((DeviceRequestPromptedEvent) event));
        this.client.on(ConnectionEvents.Target_detachedFromTarget, (ignore) -> this.client = null);
    }

    private void onDeviceRequestPrompted(DeviceRequestPromptedEvent event) {
        if (this.deviceRequestPromps.isEmpty()) return;
        Objects.requireNonNull(this.client, "Session is detached");
        CdpDeviceRequestPrompt devicePrompt = new CdpDeviceRequestPrompt(this.client, this.timeoutSettings, event);
        for (AwaitableResult<CdpDeviceRequestPrompt> subject : this.deviceRequestPromps) {
            subject.onSuccess(devicePrompt);
        }
        this.deviceRequestPromps.clear();
    }

    public CdpDeviceRequestPrompt waitForDevicePrompt() {
        return this.waitForDevicePrompt(null);
    }

    public CdpDeviceRequestPrompt waitForDevicePrompt(Integer timeout) {
        Objects.requireNonNull(this.client, "Cannot wait for device prompt through detached session!");
        boolean needsEnable = this.deviceRequestPromps.isEmpty();
        if (Objects.isNull(timeout)) {
            timeout = this.timeoutSettings.timeout();
        }
        AwaitableResult<CdpDeviceRequestPrompt> promptAwaitableResult = AwaitableResult.create();
        try {
            this.deviceRequestPromps.add(promptAwaitableResult);
            if (needsEnable) {
                this.client.send("DeviceAccess.enable");
            }
            boolean waiting = promptAwaitableResult.waiting(timeout, TimeUnit.MILLISECONDS);
            if (!waiting) {
                throw new TimeoutException("Waiting for `DeviceRequestPrompt` failed: " + timeout + "ms exceeded");
            }
            return promptAwaitableResult.get();
        } finally {
            this.deviceRequestPromps.remove(promptAwaitableResult);
        }
    }
}
