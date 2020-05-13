package com.ruiyun.jvppeteer.core.page;

import com.ruiyun.jvppeteer.protocol.input.ClickablePoint;

import java.util.List;

public class BoxModel {

    private List<ClickablePoint> content;

    private List<ClickablePoint> padding;

    private List<ClickablePoint> border;

    private List<ClickablePoint> margin;
    private int width;

    private int height;

    public BoxModel(List<ClickablePoint> content, List<ClickablePoint> padding, List<ClickablePoint> border, List<ClickablePoint> margin, int width, int height) {
        this.content = content;
        this.padding = padding;
        this.border = border;
        this.margin = margin;
        this.width = width;
        this.height = height;
    }

    public BoxModel() {
    }

    public List<ClickablePoint> getContent() {
        return content;
    }

    public void setContent(List<ClickablePoint> content) {
        this.content = content;
    }

    public List<ClickablePoint> getPadding() {
        return padding;
    }

    public void setPadding(List<ClickablePoint> padding) {
        this.padding = padding;
    }

    public List<ClickablePoint> getBorder() {
        return border;
    }

    public void setBorder(List<ClickablePoint> border) {
        this.border = border;
    }

    public List<ClickablePoint> getMargin() {
        return margin;
    }

    public void setMargin(List<ClickablePoint> margin) {
        this.margin = margin;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
