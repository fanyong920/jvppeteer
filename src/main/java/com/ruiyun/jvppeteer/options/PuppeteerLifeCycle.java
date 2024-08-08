package com.ruiyun.jvppeteer.options;

public enum PuppeteerLifeCycle {
    NETWORKIDLE("networkidle"),//不再有网络连接时触发（至少500毫秒后）
    LOAD("load"),//页面的load事件触发时
    DOMCONTENTLOADED("domcontentloaded"),//页面的DOMContentLoaded事件触发时
    NETWORKIDLE2("networkidle2");//只有2个网络连接时触发（至少500毫秒后）
    private String value;
    PuppeteerLifeCycle(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
