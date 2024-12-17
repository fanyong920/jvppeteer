package com.ruiyun.jvppeteer.common;

public enum Product {
    Chromium("chromium"),
    Chrome("chrome"),
    Chromedriver("chromedriver"),
    Firefox("firefox"),
    Chrome_headless_shell("chrome-headless-shell");
    private final String product;

    Product(String product) {
        this.product = product;
    }

    public String getProduct() {
        return product;
    }
}
