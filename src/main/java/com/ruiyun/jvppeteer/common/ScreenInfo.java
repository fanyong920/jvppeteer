package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.cdp.entities.ScreenOrientation;

public class ScreenInfo {
    private int left;
    private int top;
    private int width;
    private int height;
    private int availLeft;
    private int availTop;
    private int availWidth;
    private int availHeight;
    private double devicePixelRatio;
    private int colorDepth;
    private ScreenOrientation orientation;
    private boolean isExtended;
    private boolean isInternal;
    private boolean isPrimary;
    private String label;
    private String id;

    // Constructors
    public ScreenInfo() {
    }

    // Getters and setters for all fields
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

    public int getAvailLeft() {
        return availLeft;
    }

    public void setAvailLeft(int availLeft) {
        this.availLeft = availLeft;
    }

    public int getAvailTop() {
        return availTop;
    }

    public void setAvailTop(int availTop) {
        this.availTop = availTop;
    }

    public int getAvailWidth() {
        return availWidth;
    }

    public void setAvailWidth(int availWidth) {
        this.availWidth = availWidth;
    }

    public int getAvailHeight() {
        return availHeight;
    }

    public void setAvailHeight(int availHeight) {
        this.availHeight = availHeight;
    }

    public double getDevicePixelRatio() {
        return devicePixelRatio;
    }

    public void setDevicePixelRatio(double devicePixelRatio) {
        this.devicePixelRatio = devicePixelRatio;
    }

    public int getColorDepth() {
        return colorDepth;
    }

    public void setColorDepth(int colorDepth) {
        this.colorDepth = colorDepth;
    }

    public ScreenOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(ScreenOrientation orientation) {
        this.orientation = orientation;
    }

    public boolean isExtended() {
        return isExtended;
    }

    public void setExtended(boolean extended) {
        isExtended = extended;
    }

    public boolean getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(boolean internal) {
        isInternal = internal;
    }

    public boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(boolean primary) {
        isPrimary = primary;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ScreenInfo{" +
                "left=" + left +
                ", top=" + top +
                ", width=" + width +
                ", height=" + height +
                ", availLeft=" + availLeft +
                ", availTop=" + availTop +
                ", availWidth=" + availWidth +
                ", availHeight=" + availHeight +
                ", devicePixelRatio=" + devicePixelRatio +
                ", colorDepth=" + colorDepth +
                ", orientation=" + orientation +
                ", isExtended=" + isExtended +
                ", isInternal=" + isInternal +
                ", isPrimary=" + isPrimary +
                ", label='" + label + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
