package com.ruiyun.jvppeteer.entities;

import java.util.Map;

/**
 * Detailed information about exception (or error) that was thrown during script compilation or
 * execution.
 */
public class ExceptionDetails {
    /**
     * Exception id.
     */
    private int exceptionId;
    /**
     * Exception text, which should be used together with exception object when available.
     */
    private String text;
    /**
     * Line number of the exception location (0-based).
     */
    private int lineNumber;
    /**
     * Column number of the exception location (0-based).
     */
    private int columnNumber;
    /**
     * Script ID of the exception location.
     */
    private String scriptId;
    /**
     * URL of the exception location, to be used when the script was not reported.
     */
    private String url;
    /**
     * JavaScript stack trace if available.
     */
    private StackTrace stackTrace;
    /**
     * Exception object if available.
     */
    private RemoteObject exception;
    /**
     * Identifier of the context where exception happened.
     */
    private int executionContextId;

    /**
     * 包含客户端关联的元数据条目的字典 但此例外，例如有关关联网络的信息 请求等
     */
    private Map<String, Object> exceptionMetaData;

    public Map<String, Object> getExceptionMetaData() {
        return exceptionMetaData;
    }

    public void setExceptionMetaData(Map<String, Object> exceptionMetaData) {
        this.exceptionMetaData = exceptionMetaData;
    }

    public int getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(int exceptionId) {
        this.exceptionId = exceptionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    public RemoteObject getException() {
        return exception;
    }

    public void setException(RemoteObject exception) {
        this.exception = exception;
    }

    public int getExecutionContextId() {
        return executionContextId;
    }

    public void setExecutionContextId(int executionContextId) {
        this.executionContextId = executionContextId;
    }
}
