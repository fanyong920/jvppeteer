package com.ruiyun.jvppeteer.common;

public class BrowserRevision {

    private final static String CHROME_VERSION = "133.0.6943.98";
    private final static String FIREFOX_VERSION = "stable_135.0";

    /**
     * 获取默认浏览器版本，最好使用默认指定的版本，否则有些cdp api参数会失效
     *
     * @return 默认的浏览器版本
     */
    public static String getVersion(Product product) {
        if (product == null) {
            throw new NullPointerException("product is null");
        }
        if (Product.Chrome.equals(product) || Product.Chromium.equals(product) || Product.Chrome_headless_shell.equals(product) || Product.Chromedriver.equals(product)) {
            return CHROME_VERSION;
        } else {
            return FIREFOX_VERSION;
        }
    }

}
