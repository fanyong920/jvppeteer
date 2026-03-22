package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.common.WindowState;

public class SetClientWindowStateParameters {
    private String clientWindow;
    private WindowState state;
    private Integer width;
    private Integer height;
    private Integer x;
    private Integer y;

    // 构造函数：用于命名状态（如 fullscreen, minimized, maximized）
    public SetClientWindowStateParameters(String clientWindow, WindowState state) {
        this.clientWindow = clientWindow;
        this.state = state;
        this.width = null;
        this.height = null;
        this.x = null;
        this.y = null;
    }

    // 构造函数：用于矩形状态（normal 状态带位置和尺寸）
    public SetClientWindowStateParameters(String clientWindow, Integer width, Integer height, Integer x, Integer y) {
        this.clientWindow = clientWindow;
        this.state = WindowState.Normal; // 对于矩形状态，默认为 normal
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }

    public String getClientWindow() {
        return clientWindow;
    }

    public void setClientWindow(String clientWindow) {
        this.clientWindow = clientWindow;
    }

    public WindowState getState() {
        return state;
    }

    public void setState(WindowState state) {
        this.state = state;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
