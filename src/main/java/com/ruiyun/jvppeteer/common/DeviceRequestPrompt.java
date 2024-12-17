package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.cdp.entities.DeviceRequestPromptDevice;
import com.ruiyun.jvppeteer.cdp.events.DeviceRequestPromptedEvent;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * DeviceRequestPrompt 实例通过 Page.waitForDevicePrompt() 方法返回。
 */
public class DeviceRequestPrompt {
    private CDPSession client;
    private final TimeoutSettings timeoutSettings;
    private final String id;
    private boolean handled;
    final Set<WaitForDevicePromise> waitForDevicePromises = new HashSet<>();
    final List<DeviceRequestPromptDevice> devices = new ArrayList<>();
    private final Consumer<DeviceRequestPromptedEvent> updateDevicesHandle = this::updateDevicesHandle;

    public DeviceRequestPrompt(CDPSession client, TimeoutSettings timeoutSettings, DeviceRequestPromptedEvent firstEvent) {
        this.client = client;
        this.timeoutSettings = timeoutSettings;
        this.id = firstEvent.getId();
        this.client.on(ConnectionEvents.DeviceAccess_deviceRequestPrompted, this.updateDevicesHandle);
        this.client.on(ConnectionEvents.Target_detachedFromTarget, (ignore) -> this.client = null);
    }

    private void updateDevicesHandle(DeviceRequestPromptedEvent event) {
        if (!this.id.equals(event.getId())) {
            return;
        }
        for (DeviceRequestPromptDevice rawDevice : event.getDevices()) {
            if (this.devices.stream().anyMatch(device -> device.getId().equals(rawDevice.getId()))) {
                continue;
            }
            DeviceRequestPromptDevice newDevice = new DeviceRequestPromptDevice(rawDevice.getId(), rawDevice.getName());
            this.devices.add(newDevice);
            for (WaitForDevicePromise waitForDevicePromise : this.waitForDevicePromises) {
                if (waitForDevicePromise.getFilter().apply(newDevice)) {
                    waitForDevicePromise.getPromise().onSuccess(newDevice);
                }
            }
        }
    }

    /**
     * 等待设备请求提示设备
     * 该方法用于在一组设备中查找符合特定条件的设备如果在指定的超时时间内没有找到符合条件的设备，则返回null
     *
     * @param filter  用于筛选设备的函数，需要返回true以表示找到的设备符合条件
     * @param timeout 等待设备的超时时间（以毫秒为单位）如果为null，则使用默认超时时间
     * @return 返回找到的符合条件的设备，如果没有找到则返回
     */
    public DeviceRequestPromptDevice waitForDevice(Function<DeviceRequestPromptDevice, Boolean> filter, Integer timeout) {
        for (DeviceRequestPromptDevice device : this.devices) {
            if (filter.apply(device)) {
                return device;
            }
        }
        if (timeout == null) {
            timeout = timeoutSettings.timeout();
        }
        AwaitableResult<DeviceRequestPromptDevice> waitableResult = AwaitableResult.create();
        WaitForDevicePromise handle = new WaitForDevicePromise(filter, waitableResult);
        this.waitForDevicePromises.add(handle);
        try {
            return waitableResult.waitingGetResult(timeout, TimeUnit.MILLISECONDS);
        } finally {
            this.waitForDevicePromises.remove(handle);
        }
    }

    /**
     * 在提示列表中选择一个设备。
     *
     * @param device 要选择的设备
     */
    public void select(DeviceRequestPromptDevice device) {
        Objects.requireNonNull(this.client, "Cannot select device through detached session!");
        ValidateUtil.assertArg(this.devices.contains(device), "Cannot select unknown device!");
        ValidateUtil.assertArg(!this.handled, "Cannot select DeviceRequestPrompt which is already handled!");
        this.client.off(ConnectionEvents.DeviceAccess_deviceRequestPrompted, this.updateDevicesHandle);
        this.handled = true;
        this.client.send("DeviceAccess.selectPrompt", new HashMap<String, Object>() {{
            put("id", DeviceRequestPrompt.this.id);
            put("deviceId", device.getId());
        }});
    }

    /**
     * 取消设备请求提示
     * <p>
     * 此方法用于取消一个尚未处理的设备请求提示如果提示已经处理，则不允许取消
     * 通过调用此方法，会向CDP会话发送取消请求，并更新当前请求的状态
     */
    public void cancel() {
         Objects.requireNonNull(this.client, "Cannot cancel prompt through detached session!");
        ValidateUtil.assertArg(!this.handled, "Cannot cancel DeviceRequestPrompt which is already handled!");
        this.client.off(ConnectionEvents.DeviceAccess_deviceRequestPrompted, this.updateDevicesHandle);
        this.handled = true;
        this.client.send("DeviceAccess.cancelPrompt", new HashMap<String, Object>() {{
            put("id", DeviceRequestPrompt.this.id);
        }});
    }

    public static class WaitForDevicePromise {
        private Function<DeviceRequestPromptDevice, Boolean> filter;
        private AwaitableResult<DeviceRequestPromptDevice> promise;

        public WaitForDevicePromise() {
        }

        public WaitForDevicePromise(Function<DeviceRequestPromptDevice, Boolean> function, AwaitableResult<DeviceRequestPromptDevice> promise) {
            this.filter = function;
            this.promise = promise;
        }

        public Function<DeviceRequestPromptDevice, Boolean> getFilter() {
            return filter;
        }

        public void setFilter(Function<DeviceRequestPromptDevice, Boolean> filter) {
            this.filter = filter;
        }

        public AwaitableResult<DeviceRequestPromptDevice> getPromise() {
            return promise;
        }

        public void setPromise(AwaitableResult<DeviceRequestPromptDevice> promise) {
            this.promise = promise;
        }

    }
}
