package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.DeviceRequestPromptDevice;
import java.util.List;

public class DeviceRequestPromptedEvent {
    private String id;
    private List<DeviceRequestPromptDevice> devices;

    public DeviceRequestPromptedEvent() {
    }

    public DeviceRequestPromptedEvent(String id, List<DeviceRequestPromptDevice> deviceId) {
        this.id = id;
        this.devices = deviceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DeviceRequestPromptDevice> getDevices() {
        return this.devices;
    }

    public void setDevices(List<DeviceRequestPromptDevice> devices) {
        this.devices = devices;
    }

    @Override
    public String toString() {
        return "DeviceRequestPromptedEvent{" +
                "id='" + id + '\'' +
                ", devices=" + devices +
                '}';
    }
}
