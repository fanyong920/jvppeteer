package com.ruiyun.jvppeteer.entities;

import java.util.List;

/**
 * JavaScript 的 CoverageEntry 类
 */
public class JSCoverageEntry extends CoverageEntry {
    /**
     * 原始 V8 脚本覆盖率条目。
     */
    ScriptCoverage rawScriptCoverage;

    public ScriptCoverage getRawScriptCoverage() {
        return rawScriptCoverage;
    }

    public void setRawScriptCoverage(ScriptCoverage rawScriptCoverage) {
        this.rawScriptCoverage = rawScriptCoverage;
    }

    public JSCoverageEntry(ScriptCoverage rawScriptCoverage) {
        this.rawScriptCoverage = rawScriptCoverage;
    }

    public JSCoverageEntry(String url, List<Range> ranges, String text, ScriptCoverage rawScriptCoverage) {
        super(url, ranges, text);
        this.rawScriptCoverage = rawScriptCoverage;
    }

    @Override
    public String toString() {
        return "JSCoverageEntry{" +
                "rawScriptCoverage=" + rawScriptCoverage +
                '}';
    }
}
