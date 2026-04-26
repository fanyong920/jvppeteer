package com.ruiyun.jvppeteer.cdp.events;

public class ProtocolWebMCPToolInvokedEvent {
    /**
     * 被调用的工具名称
     */
    private String toolName;

    /**
     * 工具所属的帧 ID
     */
    private String frameId;

    /**
     * 调用标识符，用于追踪此次调用
     */
    private String invocationId;

    /**
     * 输入参数（JSON 字符串格式）
     */
    private String input;

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getInvocationId() {
        return invocationId;
    }

    public void setInvocationId(String invocationId) {
        this.invocationId = invocationId;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}
