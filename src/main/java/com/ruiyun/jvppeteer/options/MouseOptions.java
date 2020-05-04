package com.ruiyun.jvppeteer.options;

/**
 *
 */
public class MouseOptions {
    /**
     * 'none' | 'left' | 'right' | 'middle';
     */
    private String button;


    private int clickCount;

    public String getButton() {
        return button;
    }

    public void setButton(String button) {
        this.button = button;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }
}
