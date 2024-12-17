package com.ruiyun.jvppeteer.bidi.entities;

public class Header {
    private String name;
    private Value value;

    public Header(String name, Value value) {
        this.name = name;
        this.value = value;
    }

    public Header() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Header{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
