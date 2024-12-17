package com.ruiyun.jvppeteer.cdp.entities;

import java.math.BigDecimal;

public class ScreencastFrameMetadata {

    /**
     * Top offset in DIP.
     */
    private double offsetTop;
    /**
     * Page scale factor.
     */
    private double  pageScaleFactor;
    /**
     * Device screen width in DIP.
     */
    private double deviceWidth;
    /**
     * Device screen height in DIP.
     */
    private double deviceHeight;
    /**
     * Position of horizontal scroll in CSS pixels.
     */
    private double scrollOffsetX;
    /**
     * Position of vertical scroll in CSS pixels.
     */
    private double scrollOffsetY;
    /**
     * Frame swap timestamp.
     */
    private BigDecimal timestamp;

    public double getOffsetTop() {
        return offsetTop;
    }

    public void setOffsetTop(double offsetTop) {
        this.offsetTop = offsetTop;
    }

    public double getDeviceWidth() {
        return deviceWidth;
    }

    public void setDeviceWidth(double deviceWidth) {
        this.deviceWidth = deviceWidth;
    }

    public double getPageScaleFactor() {
        return pageScaleFactor;
    }

    public void setPageScaleFactor(double pageScaleFactor) {
        this.pageScaleFactor = pageScaleFactor;
    }

    public double getDeviceHeight() {
        return deviceHeight;
    }

    public void setDeviceHeight(double deviceHeight) {
        this.deviceHeight = deviceHeight;
    }

    public double getScrollOffsetX() {
        return scrollOffsetX;
    }

    public void setScrollOffsetX(double scrollOffsetX) {
        this.scrollOffsetX = scrollOffsetX;
    }

    public double getScrollOffsetY() {
        return scrollOffsetY;
    }

    public void setScrollOffsetY(double scrollOffsetY) {
        this.scrollOffsetY = scrollOffsetY;
    }

    public BigDecimal getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigDecimal timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ScreencastFrameMetadata{" +
                "offsetTop=" + offsetTop +
                ", pageScaleFactor=" + pageScaleFactor +
                ", deviceWidth=" + deviceWidth +
                ", deviceHeight=" + deviceHeight +
                ", scrollOffsetX=" + scrollOffsetX +
                ", scrollOffsetY=" + scrollOffsetY +
                ", timestamp=" + timestamp +
                '}';
    }
}
