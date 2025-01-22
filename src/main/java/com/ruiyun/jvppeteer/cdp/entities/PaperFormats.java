package com.ruiyun.jvppeteer.cdp.entities;

public enum PaperFormats {
    //21.59cm x 27.94cm
    letter(8.5, 11),
    //21.59cm x 35.56cm
    legal(8.5, 14),
    // 27.94cm x 43.18cm
    tabloid(11, 17),
    // 43.18cm x 27.94cm
    ledger(17, 11),
    // 84.1cm x 118.9cm
    a0(33.1, 46.8),
    //59.4cm x 84.1cm
    a1(23.4, 33.1),
    //42cm x 59.4cm
    a2(16.54, 23.4),
    // 29.7cm x 42cm
    a3(11.7, 16.54),
    //21cm x 29.7cm
    a4(8.27, 11.7),
    //14.8cm x 21cm
    a5(5.83, 8.27),
    //10.5cm x 14.8cm
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
