package com.ruiyun.jvppeteer.bidi.entities;

public class SetViewportParameters {

    private String context;
    private BidiViewport viewport;
    private Double devicePixelRatio;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public BidiViewport getViewport() {
        return viewport;
    }

    public void setViewport(BidiViewport viewport) {
        this.viewport = viewport;
    }

    public Double getDevicePixelRatio() {
        return devicePixelRatio;
    }

    public void setDevicePixelRatio(Double devicePixelRatio) {
        this.devicePixelRatio = devicePixelRatio;
    }
}
