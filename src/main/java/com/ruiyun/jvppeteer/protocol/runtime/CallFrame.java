package com.ruiyun.jvppeteer.protocol.runtime;

public class CallFrame {

    /**
     * JavaScript function name.
     */
    private String functionName;

    /**
     * JavaScript script id.
     */
    private String scriptId;

    /**
     * JavaScript script name or url.
     */
    private String url;

    /**
     * JavaScript script line number (0-based).
     */
    private int lineNumber;

    /**
     * JavaScript script column number (0-based).
     */
    private int columnNumber;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

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

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }
}
