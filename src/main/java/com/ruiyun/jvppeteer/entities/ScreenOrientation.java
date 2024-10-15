package com.ruiyun.jvppeteer.entities;

/**
 * Screen orientation.
 */
public class ScreenOrientation {

    /**
     * Orientation type.
     * "portraitPrimary"|"portraitSecondary"|"landscapePrimary"|"landscapeSecondary"
     */
    private String type;
    /**
     * Orientation angle.
     */
    private int angle;

    public ScreenOrientation() {
    }

    public ScreenOrientation(int angle,String type) {
        this.angle = angle;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }
}
