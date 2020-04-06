package com.ruiyun.jvppeteer.protocol;

public enum DevToolProtocol {

    JSON_LIST("/json/list");

    private String name;

    public String getName() {
        return name;
    }

    DevToolProtocol(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
