package com.ruiyun.jvppeteer.core.page;

public enum NavigateResult {

    CONTENT_SUCCESS("Content-success"),SUCCESS("success"),TIMEOUT("timeout"),TERMINATION("termination");

    private String result;

    NavigateResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}
