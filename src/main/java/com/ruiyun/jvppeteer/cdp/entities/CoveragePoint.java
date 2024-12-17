package com.ruiyun.jvppeteer.cdp.entities;

public class CoveragePoint {
    private double offset;

    private double type;

    private CoverageRange range;

    public CoveragePoint() {
        super();
    }

    public CoveragePoint(double offset, double type, CoverageRange range) {
        super();
        this.offset = offset;
        this.type = type;
        this.range = range;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public double getType() {
        return type;
    }

    public void setType(double type) {
        this.type = type;
    }

    public CoverageRange getRange() {
        return range;
    }

    public void setRange(CoverageRange range) {
        this.range = range;
    }
}
