package com.ruiyun.jvppeteer.entities;

import java.util.List;

public class BoxModel {

    private List<Point> content;

    private List<Point> padding;

    private List<Point> border;

    private List<Point> margin;

    private double width;

    private double height;

    public BoxModel(List<Point> content, List<Point> padding, List<Point> border, List<Point> margin, double width, double height) {
        this.content = content;
        this.padding = padding;
        this.border = border;
        this.margin = margin;
        this.width = width;
        this.height = height;
    }

    public BoxModel() {
    }

    public List<Point> getContent() {
        return content;
    }

    public void setContent(List<Point> content) {
        this.content = content;
    }

    public List<Point> getPadding() {
        return padding;
    }

    public void setPadding(List<Point> padding) {
        this.padding = padding;
    }

    public List<Point> getBorder() {
        return border;
    }

    public void setBorder(List<Point> border) {
        this.border = border;
    }

    public List<Point> getMargin() {
        return margin;
    }

    public void setMargin(List<Point> margin) {
        this.margin = margin;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "BoxModel{" +
                "content=" + content +
                ", padding=" + padding +
                ", border=" + border +
                ", margin=" + margin +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
