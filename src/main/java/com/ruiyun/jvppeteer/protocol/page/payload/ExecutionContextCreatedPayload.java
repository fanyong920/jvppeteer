package com.ruiyun.jvppeteer.protocol.page.payload;

import com.ruiyun.jvppeteer.protocol.context.ExecutionContextDescription;

/**
 * Issued when new execution context is created.
 */
public class ExecutionContextCreatedPayload {
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
