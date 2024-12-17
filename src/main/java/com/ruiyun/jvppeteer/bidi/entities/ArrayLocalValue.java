package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class ArrayLocalValue  extends LocalValue{
    private String type;
    private List<LocalValue> value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<LocalValue> getValue() {
        return value;
    }

    public void setValue(List<LocalValue> value) {
        this.value = value;
    }
}
