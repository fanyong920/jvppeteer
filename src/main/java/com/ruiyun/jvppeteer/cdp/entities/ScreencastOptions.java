package com.ruiyun.jvppeteer.cdp.entities;

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
    /**
     * 指定帧速率（以每秒帧数为单位）。
     * <p>
     * 默认'30' （'20' 表示 GIF）
     */
    private Integer fps;
    /**
     * 指定循环播放的次数，从 '0' 到 MAX_VALUE。
     * 值 '0' 或 'undefined' 将禁用循环。
     */
    private Integer loop;
    /**
     * 指定循环迭代之间的延迟（以毫秒为单位）。
     * '-1' 是一个特殊值，用于重复使用之前的延迟。
     */
    private Long delay;
    /**
     * 指定录制
     * <a href="https://trac.ffmpeg.org/wiki/Encode/VP9#constantq">质量</a>
     * 介于 '0'–'63' 之间的恒定速率因子。值越低意味着质量越好。
     * <p>
     * 默认 '30'
     */
    private Integer quality = 30;
    /**
     * 指定
     * <a href=https://ffmpeg.org/ffmpeg-filters.html#palettegen >调色板</a>
     * 要量化的颜色，GIF 限制为“256”。
     * 将调色板限制为仅必要的颜色以减小输出文件大小。
     * <p>
     * 默认 '256'
     */
    private Integer colors = 256;

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
                ", fps=" + fps +
                ", loop=" + loop +
                ", delay=" + delay +
                ", quality=" + quality +
                ", colors=" + colors +
                '}';
    }

    public Integer getFps() {
        return fps;
    }

    public void setFps(Integer fps) {
        this.fps = fps;
    }

    public Integer getLoop() {
        return loop;
    }

    public void setLoop(Integer loop) {
        this.loop = loop;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setColors(Integer colors) {
        this.colors = colors;
    }

    public Integer getColors() {
        return colors;
    }
}
