package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.cdp.entities.StackTrace;

public class Initiator {
    /**
     * Type of this initiator."parser"|"script"|"preload"|"SignedExchange"|"other"
     */
    private String type;
    /**
     * Initiator JavaScript stack trace, set for Script only.
     */
    private StackTrace stackTrace;

    /**
     * Initiator line number, set for Parser type or for Script type (when script is importing
     module) (0-based).
     */
    private int lineNumber;
    /**
     * Initiator column number, set for Parser type or for Script type (when script is importing
     * module) (0-based).
     */
    private int columnNumber;
    /**
     * Set if another request triggered this request (e.g. preflight).
     */
    private String request;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
