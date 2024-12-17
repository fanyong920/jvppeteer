package com.ruiyun.jvppeteer.cdp.entities;

import java.util.List;

public class StackTrace {

    /**
     * String label of this stack trace. For async traces this may be a name of the function that
     initiated the async call.
     */
    private String description;
    /**
     * JavaScript function name.
     */
    private List<CallFrame> callFrames;
    /**
     * Asynchronous JavaScript stack trace that preceded this stack, if available.
     */
    private StackTrace parent;

    /**
     * Asynchronous JavaScript stack trace that preceded this stack, if available.
     */
    private StackTraceId parentId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CallFrame> getCallFrames() {
        return callFrames;
    }

    public void setCallFrames(List<CallFrame> callFrames) {
        this.callFrames = callFrames;
    }

    public StackTrace getParent() {
        return parent;
    }

    public void setParent(StackTrace parent) {
        this.parent = parent;
    }

    public StackTraceId getParentId() {
        return parentId;
    }

    public void setParentId(StackTraceId parentId) {
        this.parentId = parentId;
    }
}
