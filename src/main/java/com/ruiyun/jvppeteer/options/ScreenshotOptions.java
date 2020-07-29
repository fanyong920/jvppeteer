package com.ruiyun.jvppeteer.options;

public class ScreenshotOptions {

    private String type;

    private String path;

    private boolean fullPage;

    private Clip clip;

    private int quality;

    private boolean omitBackground;

    private String encoding;

    public ScreenshotOptions() {
        super();
    }

    public ScreenshotOptions(String path) {
        this.path = path;
    }

    public ScreenshotOptions(String type, String path, boolean fullPage, Clip clip, int quality, boolean omitBackground, String encoding) {
        super();
        this.type = type;
        this.path = path;
        this.fullPage = fullPage;
        this.clip = clip;
        this.quality = quality;
        this.omitBackground = omitBackground;
        this.encoding = encoding;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getFullPage() {
        return fullPage;
    }

    public void setFullPage(boolean fullPage) {
        this.fullPage = fullPage;
    }

    public Clip getClip() {
        return clip;
    }

    public void setClip(Clip clip) {
        this.clip = clip;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public boolean getOmitBackground() {
        return omitBackground;
    }

    public void setOmitBackground(boolean omitBackground) {
        this.omitBackground = omitBackground;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
