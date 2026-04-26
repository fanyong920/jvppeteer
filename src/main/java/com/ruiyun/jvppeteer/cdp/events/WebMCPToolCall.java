package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.WebMCPTool;
import com.ruiyun.jvppeteer.common.Constant;

/**
 * Represents a tool call
 */

public class WebMCPToolCall {
    /**
     * Tool invocation identifier.
     */
    private String id;

    /**
     * Tool that was called.
     */
    private WebMCPTool tool;

    /**
     * The input parameters used for the call.
     */
    private Object input;

    public WebMCPToolCall(String invocationId, WebMCPTool tool, String input) {
        this.id = invocationId;
        this.tool = tool;
        this.input = Constant.OBJECTMAPPER.convertValue(input, Object.class);
    }

    public Object input() {
        return input;
    }

    public WebMCPTool tool() {
        return tool;
    }

    public String id() {
        return id;
    }


}
