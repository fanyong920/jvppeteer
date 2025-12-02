package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class RequestDevicePromptUpdatedParameters {
    private String id;
    private List<RequestDeviceInfo> devices;
    private String prompt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<RequestDeviceInfo> getDevices() {
        return devices;
    }

    public void setDevices(List<RequestDeviceInfo> devices) {
        this.devices = devices;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        return "RequestDevicePromptUpdatedParameters{" +
                "id='" + id + '\'' +
                ", devices=" + devices +
                ", prompt='" + prompt + '\'' +
                '}';
    }
}
