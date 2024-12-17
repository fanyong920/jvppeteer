package com.ruiyun.jvppeteer.bidi.entities;

public class ClipRectangle {
    private String type;
    private SharedReference element;
    private double x;
    private double y;
    private double width;
    private double height;

    public ClipRectangle() {
    }

    public ClipRectangle(String type, SharedReference element, double x, double y, double width, double height) {
        this.type = type;
        this.element = element;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SharedReference getElement() {
        return element;
    }

    public void setElement(SharedReference element) {
        this.element = element;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "ClipRectangle{" +
                "type='" + type + '\'' +
                ", element=" + element +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
