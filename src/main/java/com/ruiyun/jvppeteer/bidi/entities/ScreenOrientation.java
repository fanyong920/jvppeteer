package com.ruiyun.jvppeteer.bidi.entities;

public class ScreenOrientation {
    private ScreenOrientationNatural natural;
    private ScreenOrientationType type;

    public ScreenOrientation() {
    }

    public ScreenOrientation(ScreenOrientationNatural natural, ScreenOrientationType type) {
        this.natural = natural;
        this.type = type;
    }

    public ScreenOrientationNatural getNatural() {
        return natural;
    }

    public void setNatural(ScreenOrientationNatural natural) {
        this.natural = natural;
    }

    public ScreenOrientationType getType() {
        return type;
    }

    public void setType(ScreenOrientationType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ScreenOrientation{" +
                "natural=" + natural +
                ", type=" + type +
                '}';
    }
}
