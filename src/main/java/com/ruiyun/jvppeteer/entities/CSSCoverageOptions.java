package com.ruiyun.jvppeteer.entities;

public class CSSCoverageOptions {
    /**
     * 是否重置每次导航的覆盖范围。
     */
    private boolean resetOnNavigation = true;

    public boolean getResetOnNavigation() {
        return resetOnNavigation;
    }

    public void setResetOnNavigation(boolean resetOnNavigation) {
        this.resetOnNavigation = resetOnNavigation;
    }
}
