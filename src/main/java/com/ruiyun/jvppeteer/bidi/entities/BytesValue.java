package com.ruiyun.jvppeteer.bidi.entities;

public class BytesValue {
    private String type;
    private String value;

    public BytesValue() {
    }

    public BytesValue(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BytesValue{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
