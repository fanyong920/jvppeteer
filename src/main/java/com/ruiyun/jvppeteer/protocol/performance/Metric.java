package com.ruiyun.jvppeteer.protocol.performance;

/**
 * Run-time execution metric.
 */
public class Metric {
    /**
     * Metric name.
     */
    private String name;
    /**
     * Metric value.
     */
    private int value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
