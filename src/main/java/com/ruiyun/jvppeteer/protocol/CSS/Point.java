package com.ruiyun.jvppeteer.protocol.CSS;

import com.ruiyun.jvppeteer.protocol.profiler.CoverageRange;

public class Point {
    private int offset;

    private int type;

    private CoverageRange range;

    public Point() {
        super();
    }

    public Point(int offset, int type, CoverageRange range) {
        super();
        this.offset = offset;
        this.type = type;
        this.range = range;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public CoverageRange getRange() {
        return range;
    }

    public void setRange(CoverageRange range) {
        this.range = range;
    }
}
