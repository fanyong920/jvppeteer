package com.ruiyun.jvppeteer.entities;

public class ScreencastOptions {
    /**
     * 保存截屏视频的文件路径。
     */
    private String path;
    /**
     * 指定要裁剪的视口区域。
     */
    private BoundingBox crop;
    /**
     * 缩放输出视频。
     * <p>
     * 例如，0.5 会将输出视频的宽度和高度缩小一半。2 将使输出视频的宽度和高度加倍。
     */
    private Double scale = 1.00;
    /**
     * 指定录制的速度。
     * <p>
     * 例如，0.5 会将输出视频减慢 50%。2 将使输出视频的速度加倍
     */
    private Double speed = 1.00;
    /**
     * ffmpeg 的路径。
     * <p>
     * 如果 ffmpeg 不在你的 PATH 中，则为必需。
     */
    private String ffmpegPath;
    /**
     * 录制的格式：webm或者gif
     */
    private ScreenCastFormat format;

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

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
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

    public ScreenCastFormat getFormat() {
        return format;
    }

    public void setFormat(ScreenCastFormat format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return "ScreencastOptions{" +
                "path='" + path + '\'' +
                ", crop=" + crop +
                ", scale=" + scale +
                ", speed=" + speed +
                ", ffmpegPath='" + ffmpegPath + '\'' +
                ", format=" + format +
                '}';
    }
}
