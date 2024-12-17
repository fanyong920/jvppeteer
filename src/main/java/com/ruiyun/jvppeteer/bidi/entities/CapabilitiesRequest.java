package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class CapabilitiesRequest {
    private List<CapabilityRequest> capabilities;
    private CapabilityRequest alwaysMatch;

    public List<CapabilityRequest> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<CapabilityRequest> capabilities) {
        this.capabilities = capabilities;
    }

    public CapabilityRequest getAlwaysMatch() {
        return alwaysMatch;
    }

    public void setAlwaysMatch(CapabilityRequest alwaysMatch) {
        this.alwaysMatch = alwaysMatch;
    }
}
