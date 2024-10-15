package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.core.Mouse;

public class MouseState {
    private Point position = new Point();
    private int buttons = Mouse.MouseButtonFlag.None.getValue();

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public int getButtons() {
        return buttons;
    }

    public void setButtons(int buttons) {
        this.buttons = buttons;
    }
}
