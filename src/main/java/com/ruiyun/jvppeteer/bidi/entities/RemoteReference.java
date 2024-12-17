package com.ruiyun.jvppeteer.bidi.entities;

public class RemoteReference  extends LocalValue{
    private String sharedId;
    private String handle;

    public String getSharedId() {
        return sharedId;
    }

    public void setSharedId(String sharedId) {
        this.sharedId = sharedId;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
}
