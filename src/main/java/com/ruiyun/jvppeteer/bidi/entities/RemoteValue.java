package com.ruiyun.jvppeteer.bidi.entities;

import com.fasterxml.jackson.databind.JsonNode;

public class RemoteValue {
    private String type;
    private JsonNode value;
    private String handle;
    private String internalId;
    private String sharedId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonNode getValue() {
        return value;
    }

    public void setValue(JsonNode value) {
        this.value = value;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getSharedId() {
        return sharedId;
    }

    public void setSharedId(String sharedId) {
        this.sharedId = sharedId;
    }

    @Override
    public String toString() {
        return "RemoteValue{" +
                "type='" + type + '\'' +
                ", value=" + value +
                ", handle='" + handle + '\'' +
                ", internalId='" + internalId + '\'' +
                ", sharedId='" + sharedId + '\'' +
                '}';
    }
}
