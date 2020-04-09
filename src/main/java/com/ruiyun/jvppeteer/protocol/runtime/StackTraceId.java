package com.ruiyun.jvppeteer.protocol.runtime;

/**
 * If `debuggerId` is set stack trace comes from another debugger and can be resolved there. This
 allows to track cross-debugger calls. See `Runtime.StackTrace` and `Debugger.paused` for usages.
 */
public class StackTraceId {

    private String id;
    /**
     * Unique identifier of current debugger.
     */
    private String debuggerId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDebuggerId() {
        return debuggerId;
    }

    public void setDebuggerId(String debuggerId) {
        this.debuggerId = debuggerId;
    }
}
