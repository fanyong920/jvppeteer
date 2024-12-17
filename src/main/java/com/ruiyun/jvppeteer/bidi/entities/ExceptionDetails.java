package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.cdp.entities.StackTrace;

public class ExceptionDetails {
    private long columnNumber;
    private RemoteValue exception;
    private long lineNumber;
    private StackTrace stackTrace;
    private String text;

    public long getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(long columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public RemoteValue getException() {
        return exception;
    }

    public void setException(RemoteValue exception) {
        this.exception = exception;
    }
}
