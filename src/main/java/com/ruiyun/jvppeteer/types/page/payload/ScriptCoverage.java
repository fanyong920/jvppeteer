package com.ruiyun.jvppeteer.types.page.payload;

import java.util.List;

/**
 * Coverage data for a JavaScript script.
 */
public class ScriptCoverage {

    /**
     * JavaScript script id.
     */
    private String scriptId;
    /**
     * JavaScript script name or url.
     */
    private String url;
    /**
     * Functions contained in the script that has coverage data.
     */
    private List<FunctionCoverage> functions;

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<FunctionCoverage> getFunctions() {
        return functions;
    }

    public void setFunctions(List<FunctionCoverage> functions) {
        this.functions = functions;
    }
}
