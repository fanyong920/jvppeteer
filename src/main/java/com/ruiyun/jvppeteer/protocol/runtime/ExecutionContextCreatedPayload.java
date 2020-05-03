package com.ruiyun.jvppeteer.protocol.runtime;

import com.ruiyun.jvppeteer.protocol.runtime.ExecutionContextDescription;

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
