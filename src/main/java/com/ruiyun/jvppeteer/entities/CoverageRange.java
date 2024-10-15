package com.ruiyun.jvppeteer.entities;

/**
 * Coverage data for a source range.
 */
public class CoverageRange {
    /**
     * JavaScript script source offset for the range start.
     */
    private double startOffset;
    /**
     * JavaScript script source offset for the range end.
     */
    private double endOffset;
    /**
     * Collected execution count of the source range.
     */
    private double count;

    public CoverageRange() {
    }

    public CoverageRange(double startOffset, double endOffset, double count) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.count = count;
    }

    public double getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(double startOffset) {
        this.startOffset = startOffset;
    }

    public double getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(double endOffset) {
        this.endOffset = endOffset;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CoverageRange{" +
                "startOffset=" + startOffset +
                ", endOffset=" + endOffset +
                ", count=" + count +
                '}';
    }
}
