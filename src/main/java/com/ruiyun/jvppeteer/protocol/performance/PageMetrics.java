package com.ruiyun.jvppeteer.protocol.performance;

public class PageMetrics {

    private String title;

    private Metrics metrics;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }
}
