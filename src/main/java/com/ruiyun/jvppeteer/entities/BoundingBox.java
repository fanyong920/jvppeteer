package com.ruiyun.jvppeteer.entities;

public class BoundingBox extends Point {
    private double width;
    private double height;

    public BoundingBox() {
    }

    public BoundingBox copy(double x, double y, double width, double height) {
        return new BoundingBox(x, y, width, height);
    }

    public BoundingBox(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public BoundingBox(double x, double y, double width, double height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return super.toString() + "BoundingBox{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
