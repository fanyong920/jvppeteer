package com.ruiyun.jvppeteer.types.page;

public enum PaperFormats {

    letter(8.5, 11),
    legal(8.5, 14),
    tabloid(11, 17),
    ledger(17, 11),
    a0(33.1, 46.8),
    a1(23.4, 33.1),
    a2(16.54, 23.4),
    a3(11.7, 16.54),
    a4(8.27, 11.7),
    a5(5.83, 8.27),
    a6(4.13, 5.83);

    private double width;

    private double height;

    PaperFormats(double width, double height) {
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
}
