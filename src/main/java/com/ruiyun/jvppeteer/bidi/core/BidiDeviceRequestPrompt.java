package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.bidi.entities.RequestDeviceInfo;
import com.ruiyun.jvppeteer.cdp.entities.DeviceRequestPromptDevice;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BidiDeviceRequestPrompt extends DeviceRequestPrompt {
    private final Session session;
    private final String contextId;
    private final String promptId;

    public BidiDeviceRequestPrompt(String contextId, String promptId, Session session, List<RequestDeviceInfo> devices) {
        super();
        this.session = session;
        this.contextId = contextId;
        this.promptId = promptId;
        this.devices.addAll(devices.stream().map(device -> new DeviceRequestPromptDevice(device.getId(), StringUtil.isEmpty(device.getName()) ? "UNKNOWN" : device.getName())).collect(Collectors.toList()));
    }

    @Override
    public DeviceRequestPromptDevice waitForDevice(Function<DeviceRequestPromptDevice, Boolean> filter, Integer timeout) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void select(DeviceRequestPromptDevice device) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.contextId);
        params.put("prompt", this.promptId);
        params.put("accept", true);
        params.put("device", device.getId());
        this.session.send("bluetooth.handleRequestDevicePrompt", params);
    }

    @Override
    public void cancel() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.contextId);
        params.put("prompt", this.promptId);
        params.put("accept", false);
        this.session.send("bluetooth.handleRequestDevicePrompt", params);
    }
}
