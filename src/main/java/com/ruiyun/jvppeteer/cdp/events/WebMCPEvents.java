package com.ruiyun.jvppeteer.cdp.events;

public enum WebMCPEvents {
    /**
     *  Emitted when tools are added.
     * {@link WebMCPToolsAddedEvent}
     */
    toolsadded,
    /**
     *  Emitted when tools are removed.
     * {@link WebMCPToolsRemovedEvent}
     */
    toolsremoved,
    /**
     *  Emitted when a tool invocation starts.
     * {@link WebMCPToolCall}
     */
    toolinvoked,
    /**
     * Emitted when a tool invocation completes or fails.
     * {@link WebMCPToolCallResult}
     */
    toolresponded
}
