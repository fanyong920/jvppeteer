package com.ruiyun.jvppeteer.entities;

public class ScreenshotClip extends BoundingBox {

    private double scale = 1;

    public ScreenshotClip() {
    }

    @Override
    public ScreenshotClip copy(double x, double y, double width, double height) {
        return new ScreenshotClip(x, y, width, height);
    }

    public ScreenshotClip(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    public ScreenshotClip(double x, double y, double width, double height, double scale) {
        super(x, y, width, height);
        this.scale = scale;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
}
