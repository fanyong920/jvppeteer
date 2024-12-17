package com.ruiyun.jvppeteer.cdp.entities;

public class JSCoverageOptions {
    /**
     * 是否重置每次导航的覆盖范围。
     */
    private boolean resetOnNavigation = true;
    /**
     * 是否应报告页面生成的匿名脚本。
     */
    private boolean reportAnonymousScripts;
    /**
     * 结果是否包含原始 V8 脚本覆盖条目。
     */
    private boolean includeRawScriptCoverage;
    /**
     * 是否收集块级别的覆盖信息。如果为 true，则将在块级别收集覆盖范围（这是默认设置）。如果为 false，则将在功能级别收集覆盖率。
     */
    private boolean useBlockCoverage = true;

    public JSCoverageOptions(boolean resetOnNavigation, boolean includeRawScriptCoverage, boolean reportAnonymousScripts, boolean useBlockCoverage) {
        this.resetOnNavigation = resetOnNavigation;
        this.includeRawScriptCoverage = includeRawScriptCoverage;
        this.reportAnonymousScripts = reportAnonymousScripts;
        this.useBlockCoverage = useBlockCoverage;
    }

    public JSCoverageOptions() {
    }

    public boolean getResetOnNavigation() {
        return resetOnNavigation;
    }

    public void setResetOnNavigation(boolean resetOnNavigation) {
        this.resetOnNavigation = resetOnNavigation;
    }

    public boolean getReportAnonymousScripts() {
        return reportAnonymousScripts;
    }

    public void setReportAnonymousScripts(boolean reportAnonymousScripts) {
        this.reportAnonymousScripts = reportAnonymousScripts;
    }

    public boolean getIncludeRawScriptCoverage() {
        return includeRawScriptCoverage;
    }

    public void setIncludeRawScriptCoverage(boolean includeRawScriptCoverage) {
        this.includeRawScriptCoverage = includeRawScriptCoverage;
    }

    public boolean getUseBlockCoverage() {
        return useBlockCoverage;
    }

    public void setUseBlockCoverage(boolean useBlockCoverage) {
        this.useBlockCoverage = useBlockCoverage;
    }

    @Override
    public String toString() {
        return "JSCoverageOptions{" +
                "resetOnNavigation=" + resetOnNavigation +
                ", reportAnonymousScripts=" + reportAnonymousScripts +
                ", includeRawScriptCoverage=" + includeRawScriptCoverage +
                ", useBlockCoverage=" + useBlockCoverage +
                '}';
    }
}
