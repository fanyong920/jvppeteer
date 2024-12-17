package com.ruiyun.jvppeteer.cdp.entities;

public class ScreenRecorderOptions {
    private Double speed;
    private BoundingBox crop;
    private String path;
    private ScreenCastFormat format;
    private Double scale;
    private String ffmpegPath;

    public ScreenRecorderOptions(Double speed, BoundingBox crop, String path, ScreenCastFormat format, Double scale, String ffmpegPath) {
        this.speed = speed;
        this.crop = crop;
        this.path = path;
        this.format = format;
        this.scale = scale;
        this.ffmpegPath = ffmpegPath;
    }

    public ScreenCastFormat getFormat() {
        return format;
    }

    public void setFormat(ScreenCastFormat format) {
        this.format = format;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public BoundingBox getCrop() {
        return crop;
    }

    public void setCrop(BoundingBox crop) {
        this.crop = crop;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }
}
