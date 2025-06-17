package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.databind.JsonNode;

public class SharedReference {
    private String sharedId;
    private String handle;
    private String type;
    private String internalId;
    private JsonNode value;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public JsonNode getValue() {
        return value;
    }

    public void setValue(JsonNode value) {
        this.value = value;
    }
}
