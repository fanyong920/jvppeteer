package com.ruiyun.jvppeteer.options;

public class VisionDeficiencyState extends ActiveProperty {
    /**
     * 取值范围： "achromatopsia" | "blurredVision" | "deuteranopia" | "none" | "protanopia"| "tritanopia"
     * 默认是"none"
     */
    public VisionDeficiency visionDeficiency = VisionDeficiency.NONE;

    public VisionDeficiencyState(boolean active) {
        super(active);
    }

    public VisionDeficiencyState(boolean active, VisionDeficiency visionDeficiency) {
        super(active);
        this.visionDeficiency = visionDeficiency;
    }

    public VisionDeficiency getVisionDeficiency() {
        return visionDeficiency;
    }

    public void setVisionDeficiency(VisionDeficiency visionDeficiency) {
        this.visionDeficiency = visionDeficiency;
    }
}
