package com.ruiyun.jvppeteer.bidi.entities;

public class PrintPageParameters {
    private double height;
    private double width;

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public PrintPageParameters(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public PrintPageParameters() {
    }

    @Override
    public String toString() {
        return "PrintPageParameters{" +
                "height=" + height +
                ", width=" + width +
                '}';
    }
}
