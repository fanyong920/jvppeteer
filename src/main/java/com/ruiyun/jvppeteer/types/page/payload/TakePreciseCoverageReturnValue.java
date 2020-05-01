package com.ruiyun.jvppeteer.types.page.payload;

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
