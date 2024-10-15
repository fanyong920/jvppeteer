package com.ruiyun.jvppeteer.entities;

public class DefaultBackgroundColorState extends ActiveProperty {

    public RGBA color;

    public DefaultBackgroundColorState(boolean active, RGBA color) {
        super(active);
        this.color = color;
    }

    public RGBA getColor() {
        return color;
    }

    public void setColor(RGBA color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "DefaultBackgroundColorState{" +
                "color=" + color +
                ", active=" + active +
                '}';
    }
}
