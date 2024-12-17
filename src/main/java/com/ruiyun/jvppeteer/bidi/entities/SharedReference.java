package com.ruiyun.jvppeteer.bidi.entities;

public class SharedReference {
    private String  sharedId;
    private String handle;

    public SharedReference(String sharedId, String handle) {
        this.sharedId = sharedId;
        this.handle = handle;
    }

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
