package com.ruiyun.jvppeteer.protocol.profiler;

/**
 * Coverage data for a source range.
 */
public class CoverageRange {
    /**
     * JavaScript script source offset for the range start.
     */
    private int startOffset;
    /**
     * JavaScript script source offset for the range end.
     */
    private int endOffset;
    /**
     * Collected execution count of the source range.
     */
    private int count;

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
