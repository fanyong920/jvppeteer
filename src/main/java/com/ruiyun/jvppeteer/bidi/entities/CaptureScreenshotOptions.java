package com.ruiyun.jvppeteer.bidi.entities;

public class CaptureScreenshotOptions {
    private String context;
    private Origin origin;
    private ImageFormat format;
    private ClipRectangle clip;

    public CaptureScreenshotOptions() {
    }

    public CaptureScreenshotOptions(String context, Origin origin, ImageFormat format, ClipRectangle clip) {
        this.context = context;
        this.origin = origin;
        this.format = format;
        this.clip = clip;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public void setFormat(ImageFormat format) {
        this.format = format;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public ClipRectangle getClip() {
        return clip;
    }

    public void setClip(ClipRectangle clip) {
        this.clip = clip;
    }

    @Override
    public String toString() {
        return "CaptureScreenshotOptions{" +
                "context='" + context + '\'' +
                ", origin=" + origin +
                ", format=" + format +
                ", clip=" + clip +
                '}';
    }
}
