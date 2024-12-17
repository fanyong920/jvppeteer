package com.ruiyun.jvppeteer.cdp.entities;

/**
 * Response HTTP header entry
 */
public class HeaderEntry {

    private String name;

    private String value;

    public HeaderEntry() {
    }

    public HeaderEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        return "HeaderEntry{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
