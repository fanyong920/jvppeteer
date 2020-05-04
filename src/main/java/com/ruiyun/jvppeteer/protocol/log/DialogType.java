package com.ruiyun.jvppeteer.protocol.log;

public enum DialogType {

    Alert("alert"), BeforeUnload("beforeunload"), Confirm("confirm"), Prompt("prompt");

    private String type;

    DialogType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
