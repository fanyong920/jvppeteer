package com.ruiyun.jvppeteer.protocol.js;

public class ProtocolValue {

    private String unserializableValue;

    private String value;

    private String objectId;

    public ProtocolValue() {
        super();
    }

    public ProtocolValue(String unserializableValue, String value, String objectId) {
        super();
        this.unserializableValue = unserializableValue;
        this.value = value;
        this.objectId = objectId;
    }

    public String getUnserializableValue() {
        return unserializableValue;
    }

    public void setUnserializableValue(String unserializableValue) {
        this.unserializableValue = unserializableValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
