package com.ruiyun.jvppeteer.bidi.entities;

public class PrintMarginParameters {
    private double bottom;
    private double left;
    private double right;
    private double top;

    public PrintMarginParameters() {
    }

    public PrintMarginParameters(double bottom, double left, double right, double top) {
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.top = top;
    }

    public double getBottom() {
        return bottom;
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public double getRight() {
        return right;
    }

    public void setRight(double right) {
        this.right = right;
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        this.top = top;
    }

    @Override
    public String toString() {
        return "PrintMarginParameters{" +
                "bottom=" + bottom +
                ", left=" + left +
                ", right=" + right +
                ", top=" + top +
                '}';
    }
}
