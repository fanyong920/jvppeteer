package com.ruiyun.jvppeteer.cdp.entities;

public class PDFMargin {
    //上边距 以cm为单位
    private String top;
    /**
     * 下边距
     */
    private String bottom;

    private String left;

    private String right;

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public String getBottom() {
        return bottom;
    }

    public void setBottom(String bottom) {
        this.bottom = bottom;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }
}
