package com.ruiyun.jvppeteer.cdp.entities;

import java.math.BigDecimal;

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
    private BigDecimal value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
