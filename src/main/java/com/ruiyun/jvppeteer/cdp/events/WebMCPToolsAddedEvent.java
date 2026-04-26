package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.WebMCPTool;

import java.util.List;

public class WebMCPToolsAddedEvent {

    private List<WebMCPTool> tools;

    public List<WebMCPTool> getTools() {
        return tools;
    }

    public void setTools(List<WebMCPTool> tools) {
        this.tools = tools;
    }

}
