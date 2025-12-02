package com.ruiyun.jvppeteer.common;

public class AddScreenParams {
    private int left;
    private int top;
    private int width;
    private int height;
    private WorkAreaInsets workAreaInsets;
    private Double devicePixelRatio;
    private Integer rotation;
    private Integer colorDepth;
    private String label;
    private Boolean isInternal;


    // Getters and setters
    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
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

    public WorkAreaInsets getWorkAreaInsets() {
        return workAreaInsets;
    }

    public void setWorkAreaInsets(WorkAreaInsets workAreaInsets) {
        this.workAreaInsets = workAreaInsets;
    }

    public Double getDevicePixelRatio() {
        return devicePixelRatio;
    }

    public void setDevicePixelRatio(Double devicePixelRatio) {
        this.devicePixelRatio = devicePixelRatio;
    }

    public Integer getRotation() {
        return rotation;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }

    public Integer getColorDepth() {
        return colorDepth;
    }

    public void setColorDepth(Integer colorDepth) {
        this.colorDepth = colorDepth;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(Boolean isInternal) {
        this.isInternal = isInternal;
    }
}
