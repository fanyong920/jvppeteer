package com.ruiyun.jvppeteer.cdp.events;

import java.util.List;

public class ProtocolWebMCPToolsAddedEvent {
    private List<ProtocolWebMCPTool> tools;

    public List<ProtocolWebMCPTool> getTools() {
        return tools;
    }

    public void setTools(List<ProtocolWebMCPTool> tools) {
        this.tools = tools;
    }
}
