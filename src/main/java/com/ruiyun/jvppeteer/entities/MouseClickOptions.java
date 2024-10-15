package com.ruiyun.jvppeteer.entities;

public class MouseClickOptions extends MouseOptions {
    private int delay;
    private int count = 1;

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
}
