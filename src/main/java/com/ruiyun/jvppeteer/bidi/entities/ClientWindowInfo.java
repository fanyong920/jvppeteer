package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.common.WindowState;

public class ClientWindowInfo {
    private boolean active;
    private String clientWindow;
    private int height;
    private WindowState state; // 可以是 'fullscreen', 'maximized', 'minimized', 'normal'
    private int width;
    private int x;
    private int y;

    public ClientWindowInfo() {
    }

    public ClientWindowInfo(boolean active, String clientWindow, int height, WindowState state, int width, int x, int y) {
        this.active = active;
        this.clientWindow = clientWindow;
        this.height = height;
        this.state = state;
        this.width = width;
        this.x = x;
        this.y = y;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getClientWindow() {
        return clientWindow;
    }

    public void setClientWindow(String clientWindow) {
        this.clientWindow = clientWindow;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public WindowState getState() {
        return state;
    }

    public void setState(WindowState state) {
        this.state = state;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
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
