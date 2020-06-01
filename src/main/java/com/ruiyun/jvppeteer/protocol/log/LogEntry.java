package com.ruiyun.jvppeteer.protocol.log;

import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.protocol.runtime.StackTrace;

import java.util.List;

/**
 * Log entry.
 */
public class LogEntry {

    /**
     * Log entry source.
     * "xml"|"javascript"|"network"|"storage"|"appcache"|"rendering"|"security"|"deprecation"|"worker"|"violation"|"intervention"|"recommendation"|"other"
     */
    private String source;
    /**
     * Log entry severity.
     * "verbose"|"info"|"warning"|"error"
     */
    private String level;
    /**
     * Logged text.
     */
    private String text;
    /**
     * Timestamp when this entry was added.
     */
    private long timestamp;
    /**
     * URL of the resource if known.
     */
    private String url;
    /**
     * Line number in the resource.
     */
    private int lineNumber;
    /**
     * JavaScript stack trace.
     */
    private StackTrace stackTrace;
    /**
     * Identifier of the network request associated with this entry.
     */
    private String networkRequestId;
    /**
     * Identifier of the worker associated with this entry.
     */
    private String workerId;
    /**
     * Call arguments.
     */
    private List<RemoteObject> args;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getNetworkRequestId() {
        return networkRequestId;
    }

    public void setNetworkRequestId(String networkRequestId) {
        this.networkRequestId = networkRequestId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public List<RemoteObject> getArgs() {
        return args;
    }

    public void setArgs(List<RemoteObject> args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "source='" + source + '\'' +
                ", level='" + level + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                ", url='" + url + '\'' +
                ", lineNumber=" + lineNumber +
                ", stackTrace=" + stackTrace +
                ", networkRequestId='" + networkRequestId + '\'' +
                ", workerId='" + workerId + '\'' +
                ", args=" + args +
                '}';
    }
}
