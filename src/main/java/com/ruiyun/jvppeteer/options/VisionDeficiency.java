package com.ruiyun.jvppeteer.options;

/**
 * 视力缺陷类,设置不同级别的实力缺陷，截图有不同的效果
 *
 * @author fanyong
 */
public enum VisionDeficiency {

    ACHROMATOPSIA("achromatopsia"),

    DEUTERANOPIA("deuteranopia"),

    PROTANOPIA("protanopia"),

    TRITANOPIA("tritanopia"),

    BLURREDVISION("blurredVision"),

    NONE("none");

    private String value;

    VisionDeficiency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
