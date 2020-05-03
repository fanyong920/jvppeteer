package com.ruiyun.jvppeteer.protocol.profiler;

import com.ruiyun.jvppeteer.protocol.profiler.ScriptCoverage;

import java.util.List;

public class TakePreciseCoverageReturnValue {

    /**
     * Coverage data for the current isolate.
     */
    private List<ScriptCoverage> result;

    public List<ScriptCoverage> getResult() {
        return result;
    }

    public void setResult(List<ScriptCoverage> result) {
        this.result = result;
    }
}
