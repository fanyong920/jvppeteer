package com.ruiyun.jvppeteer.common;

public class ScreenOrientation {
    private int angle;
    private String type;

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ScreenOrientation{" +
                "angle=" + angle +
                ", type='" + type + '\'' +
                '}';
    }
}
