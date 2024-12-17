package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

public class AdFrameStatus {
    private List<String> explanations;
    private String adFrameType;

    public List<String> getExplanations() {
        return explanations;
    }

    public void setExplanations(List<String> explanations) {
        this.explanations = explanations;
    }

    public String getAdFrameType() {
        return adFrameType;
    }

    public void setAdFrameType(String adFrameType) {
        this.adFrameType = adFrameType;
    }

    @Override
    public String toString() {
        return "AdFrameStatus{" +
                "explanations=" + explanations +
                ", adFrameType='" + adFrameType + '\'' +
                '}';
    }
}
