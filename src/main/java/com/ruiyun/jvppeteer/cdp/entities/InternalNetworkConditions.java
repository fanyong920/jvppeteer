package com.ruiyun.jvppeteer.cdp.entities;

public class InternalNetworkConditions extends NetworkConditions {

    public InternalNetworkConditions(boolean offline, double upload, double download, double latency) {
        super(upload, download, latency);
        super.setOffline(offline);
    }

    public InternalNetworkConditions() {
        super();
    }

}
