package com.ruiyun.jvppeteer.entities;

public class InternalNetworkConditions extends NetworkConditions {
    private boolean offline;

    public InternalNetworkConditions(boolean offline, double upload, double download, double latency) {
        super(upload, download, latency);
        this.offline = offline;
    }

    public boolean getOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public InternalNetworkConditions() {
        super();
        this.offline = false;
    }


}
