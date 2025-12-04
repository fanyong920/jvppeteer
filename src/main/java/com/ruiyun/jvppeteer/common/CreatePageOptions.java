package com.ruiyun.jvppeteer.common;

public class CreatePageOptions {
    private CreateType type;
    private WindowBounds windowBounds;

    public CreateType getType() {
        return type;
    }

    public void setType(CreateType type) {
        this.type = type;
    }

    public WindowBounds getWindowBounds() {
        return windowBounds;
    }

    public void setWindowBounds(WindowBounds windowBounds) {
        this.windowBounds = windowBounds;
    }

    @Override
    public String toString() {
        return "CreatePageOptions{" +
                "type=" + type +
                ", windowBounds=" + windowBounds +
                '}';
    }
}
