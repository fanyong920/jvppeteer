package com.ruiyun.jvppeteer.entities;

public class CpuThrottlingState extends ActiveProperty {

    public Double factor;

    public CpuThrottlingState(boolean active) {
        super(active);
    }

    public CpuThrottlingState(boolean active, Double factor) {
        super(active);
        this.factor = factor;
    }

    public Double getFactor() {
        return factor;
    }

    public void setFactor(Double factor) {
        this.factor = factor;
    }
}
