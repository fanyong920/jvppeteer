package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.StackTrace;

public class ProtocolWebMCPTool {
    private String name;
    private String description;
    private Object inputSchema;
    private WebMCPAnnotation annotations;
    private String frameId;
    private Integer backendNodeId;
    private StackTrace stackTrace;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(Object inputSchema) {
        this.inputSchema = inputSchema;
    }

    public WebMCPAnnotation getAnnotations() {
        return annotations;
    }

    public void setAnnotations(WebMCPAnnotation annotations) {
        this.annotations = annotations;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public Integer getBackendNodeId() {
        return backendNodeId;
    }

    public void setBackendNodeId(Integer backendNodeId) {
        this.backendNodeId = backendNodeId;
    }

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }
}
