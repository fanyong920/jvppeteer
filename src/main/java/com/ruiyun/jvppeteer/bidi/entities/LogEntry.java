package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.cdp.entities.StackTrace;
import java.math.BigDecimal;
import java.util.List;

public class LogEntry {

    private Source source;
    /**
     * Log entry severity.
     * "debug"|"info"|"warn"|"error"
     */
    private String level;
    /**
     * Logged text.
     */
    private String text;

    private String category;
    /**
     * Timestamp when this entry was added.
     */
    private BigDecimal timestamp;
    /**
     * /**
     * JavaScript stack trace.
     */
    private StackTrace stackTrace;

    private String type;

    private String method;

    public List<RemoteValue> getArgs() {
        return args;
    }

    public void setArgs(List<RemoteValue> args) {
        this.args = args;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public BigDecimal getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigDecimal timestamp) {
        this.timestamp = timestamp;
    }

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Call arguments.
     */
    private List<RemoteValue> args;

    @Override
    public String toString() {
        return "LogEntry{" +
                "source=" + source +
                ", level='" + level + '\'' +
                ", text='" + text + '\'' +
                ", category='" + category + '\'' +
                ", timestamp=" + timestamp +
                ", stackTrace=" + stackTrace +
                ", type='" + type + '\'' +
                ", method='" + method + '\'' +
                ", args=" + args +
                '}';
    }
}
