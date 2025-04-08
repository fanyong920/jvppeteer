package com.ruiyun.jvppeteer.cdp.entities;

import java.util.Objects;

public class ScreenRecorderOptions {
    public static final int DEFAULT_FPS = 30;
    public static final int CRF_VALUE = 30;
    private Integer loop;
    private Integer quality;
    private Long delay;
    private Integer colors;
    private Integer fps;
    private Double speed;
    private BoundingBox crop;
    private String path;
    private ScreenCastFormat format;
    private Double scale;
    private String ffmpegPath;

    public ScreenRecorderOptions(Double speed, BoundingBox crop, String path, ScreenCastFormat format, Double scale, String ffmpegPath, Integer fps, Integer loop, Long delay, Integer quality, Integer colors) {
        this.speed = speed;
        this.crop = crop;
        this.path = path;
        this.format = Objects.isNull(format) ? ScreenCastFormat.WEBM : format;
        this.scale = scale;
        this.ffmpegPath = ffmpegPath;
        this.fps = Objects.isNull(fps) ? Objects.equals(this.format, ScreenCastFormat.GIF) ? 20 : DEFAULT_FPS : fps;
        this.loop = Objects.isNull(loop) || loop == 0 ? -1 : loop;
        this.delay = Objects.isNull(delay) ? -1 : delay;
        this.quality = Objects.isNull(quality) ? CRF_VALUE : quality;
        this.colors = Objects.isNull(colors) ? 256 : colors;
        ;
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

    public Integer getLoop() {
        return loop;
    }

    public Long getDelay() {
        return delay;
    }

    public Integer getColors() {
        return colors;
    }

    public Integer getFps() {
        return fps;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setLoop(Integer loop) {
        this.loop = loop;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public void setColors(Integer colors) {
        this.colors = colors;
    }

    public void setFps(Integer fps) {
        this.fps = fps;
    }
}
