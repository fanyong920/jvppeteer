package com.ruiyun.jvppeteer.common;

public class CreatePageOptions {
    private CreateType type;
    private WindowBounds windowBounds;
    /**
     * Whether to create the page in the background.
     *
     */
    private Boolean background;

    public CreateType getType() {
        return type;
    }

    public void setType(CreateType type) {
        this.type = type;
    }

    public WindowBounds getWindowBounds() {
        return windowBounds;
    }

    public void setWindowBounds(WindowBounds windowBounds) {
        this.windowBounds = windowBounds;
    }

    public Boolean getBackground() {
        return background;
    }

    public void setBackground(Boolean background) {
        this.background = background;
    }
}
