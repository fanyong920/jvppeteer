package com.ruiyun.jvppeteer.common;

public enum Product {
    CHROMIUM("chromium"),
    CHROME("chrome"),
    CHROMEDRIVER("chromedriver"),
    FIREFOX("firefox"),
    CHROMEHEADLESSSHELL("chrome-headless-shell");
    private final String product;

    Product(String product) {
        this.product = product;
    }

    public String getProduct() {
        return product;
    }
}
