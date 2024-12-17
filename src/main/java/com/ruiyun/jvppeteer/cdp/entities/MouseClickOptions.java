package com.ruiyun.jvppeteer.cdp.entities;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MouseClickOptions extends MouseOptions {
    private int delay;
    private int count = 1;
    //bidi 使用
    private ObjectNode origin;
    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ObjectNode getOrigin() {
        return origin;
    }

    public void setOrigin(ObjectNode origin) {
        this.origin = origin;
    }
}
