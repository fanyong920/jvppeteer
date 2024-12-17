package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.ExecutionContextDescription;

/**
 * Issued when new execution context is created.
 */
public class ExecutionContextCreatedEvent {
    /**
     * A newly created execution context.
     */
    private ExecutionContextDescription context;

    public ExecutionContextDescription getContext() {
        return context;
    }

    public void setContext(ExecutionContextDescription context) {
        this.context = context;
    }

}
