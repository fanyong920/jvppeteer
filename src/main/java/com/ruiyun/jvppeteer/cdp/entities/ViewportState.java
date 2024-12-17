package com.ruiyun.jvppeteer.cdp.entities;

public class ViewportState extends ActiveProperty{

    public Viewport viewport;

    public ViewportState(boolean active, Viewport viewport) {
        super(active);
        this.viewport = viewport;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }
}
