package com.ruiyun.jvppeteer.cdp.entities;

public class MouseWheelOptions {
    /**
     * 鼠标滚轮事件的 X 增量（以 CSS 像素为单位）（默认值：0）
     */
    private double deltaX;
    /**
     * 鼠标滚轮事件的 Y 增量（以 CSS 像素为单位）（默认值：0）。
     */
    private double deltaY;

    public double getDeltaX() {
        return deltaX;
    }

    public void setDeltaX(double deltaX) {
        this.deltaX = deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public void setDeltaY(double deltaY) {
        this.deltaY = deltaY;
    }
}
