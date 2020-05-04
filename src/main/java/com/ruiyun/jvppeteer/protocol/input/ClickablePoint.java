package com.ruiyun.jvppeteer.protocol.input;

public class ClickablePoint {
    private int x;
    private int y;

    public ClickablePoint() {
    }

    public ClickablePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
