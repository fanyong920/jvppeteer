package com.ruiyun.jvppeteer.cdp.entities;

public class ClientSecurityState {
    private boolean initiatorIsSecureContext;
    private IPAddressSpace initiatorIPAddressSpace;
    private PrivateNetworkRequestPolicy privateNetworkRequestPolicy;

    public boolean isInitiatorIsSecureContext() {
        return initiatorIsSecureContext;
    }

    public void setInitiatorIsSecureContext(boolean initiatorIsSecureContext) {
        this.initiatorIsSecureContext = initiatorIsSecureContext;
    }

    public PrivateNetworkRequestPolicy getPrivateNetworkRequestPolicy() {
        return privateNetworkRequestPolicy;
    }

    public void setPrivateNetworkRequestPolicy(PrivateNetworkRequestPolicy privateNetworkRequestPolicy) {
        this.privateNetworkRequestPolicy = privateNetworkRequestPolicy;
    }

    public IPAddressSpace getInitiatorIPAddressSpace() {
        return initiatorIPAddressSpace;
    }

    public void setInitiatorIPAddressSpace(IPAddressSpace initiatorIPAddressSpace) {
        this.initiatorIPAddressSpace = initiatorIPAddressSpace;
    }

    @Override
    public String toString() {
        return "ClientSecurityState{" +
                "initiatorIsSecureContext=" + initiatorIsSecureContext +
                ", initiatorIPAddressSpace=" + initiatorIPAddressSpace +
                ", privateNetworkRequestPolicy=" + privateNetworkRequestPolicy +
                '}';
    }
}
