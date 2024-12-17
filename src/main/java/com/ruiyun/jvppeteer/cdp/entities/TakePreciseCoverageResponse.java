package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

public class TakePreciseCoverageResponse {

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
