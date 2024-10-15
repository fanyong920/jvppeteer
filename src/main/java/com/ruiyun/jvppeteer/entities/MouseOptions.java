package com.ruiyun.jvppeteer.entities;

/**
 *
 */
public class MouseOptions {
    /**
     * 'none' | 'left' | 'right' | 'middle';
     */
    private String button = "left";


    private int clickCount = 1;

    public MouseOptions() {
        super();
    }


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
