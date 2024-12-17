package com.ruiyun.jvppeteer.bidi.entities;

public class LocalValue {
    private String type;
    private Object value;

    public LocalValue(String type, Object value) {
        this.type = type;
        this.value = value;
    }


    public LocalValue() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
