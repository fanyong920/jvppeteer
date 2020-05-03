package com.ruiyun.jvppeteer.protocol.profiler;

import java.util.List;

/**
 * Coverage data for a JavaScript function.
 */

public class FunctionCoverage {

    /**
     * JavaScript function name.
     */
    private String functionName;
    /**
     * Source ranges inside the function with coverage data.
     */
    private List<CoverageRange> ranges;
    /**
     * Whether coverage data for this function has block granularity.
     */
    private boolean isBlockCoverage;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public List<CoverageRange> getRanges() {
        return ranges;
    }

    public void setRanges(List<CoverageRange> ranges) {
        this.ranges = ranges;
    }

    public boolean getIsBlockCoverage() {
        return isBlockCoverage;
    }

    public void setIsBlockCoverage(boolean isBlockCoverage) {
        this.isBlockCoverage = isBlockCoverage;
    }
}
