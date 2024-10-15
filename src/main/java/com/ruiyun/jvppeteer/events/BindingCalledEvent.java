package com.ruiyun.jvppeteer.events;

public class BindingCalledEvent {

    private String name;

    private String payload;
    /**
     * Identifier of the context where the call was made.
     */
    private int executionContextId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getExecutionContextId() {
        return executionContextId;
    }

    public void setExecutionContextId(int executionContextId) {
        this.executionContextId = executionContextId;
    }
}
