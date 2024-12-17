package com.ruiyun.jvppeteer.bidi.entities;

public class ChannelValue extends LocalValue{
    private String type;
    private ChannelProperties value;

    public ChannelValue(String type, Object value, String type1, ChannelProperties value1) {
        super(type, value);
        this.type = type1;
        this.value = value1;
    }

    public ChannelValue(String type, ChannelProperties value) {
        this.type = type;
        this.value = value;
    }

    public ChannelProperties getValue() {
        return value;
    }

    public void setValue(ChannelProperties value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
