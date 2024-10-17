package com.ruiyun.jvppeteer.entities;

import java.math.BigDecimal;

public class Timestamp {
    private BigDecimal value;

    public Timestamp() {
    }

    public Timestamp(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

}
