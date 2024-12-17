package com.ruiyun.jvppeteer.bidi.entities;

public class ImageFormat {
    private String type;
    private Double quality;

    public ImageFormat() {
    }

    public ImageFormat(String type, Double quality) {
        this.type = type;
        this.quality = quality;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getQuality() {
        return quality;
    }

    public void setQuality(Double quality) {
        this.quality = quality;
    }

    @Override
    public String toString() {
        return "ImageFormat{" +
                "type='" + type + '\'' +
                ", quality=" + quality +
                '}';
    }
}
