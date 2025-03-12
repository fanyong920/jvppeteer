package com.ruiyun.jvppeteer.cdp.entities;

public class CompoundPSelector {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public CompoundPSelector(String value) {
        this.value = value;
    }

    public CompoundPSelector(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
