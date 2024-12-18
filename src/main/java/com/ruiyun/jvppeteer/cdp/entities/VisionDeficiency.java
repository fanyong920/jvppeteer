package com.ruiyun.jvppeteer.cdp.entities;

/**
 * 视力缺陷类,设置不同级别的实力缺陷，截图有不同的效果
 *
 * @author fanyong
 */
public enum VisionDeficiency {
    /**
     * 用户无法感知任何颜色，这会将所有颜色减少为灰色阴影
     */
    Achromatopsia("achromatopsia"),
    /**
     * 	用户无法感知任何绿灯。
     */
    Deuteranopia("deuteranopia"),
    /**
     * 用户无法感知任何红灯。
     */
    Protanopia("protanopia"),
    /**
     * 用户无法感知任何蓝光。
     */
    Tritanopia("tritanopia"),
    /**
     * 用户难以专注于精细细节。
     */
    BlurredVision("blurredVision"),

    reducedContrast("reducedContrast"),
    /**
     * 不应用任何颜色偏差
     */
    NONE("none");

    private final String value;

    VisionDeficiency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
