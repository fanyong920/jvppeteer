package com.ruiyun.jvppeteer.entities;

import java.util.List;

public class GetMetricsResponse {
    List<Metric> metrics;

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }
}
