package com.ruiyun.jvppeteer.types.page.payload;

/**
 * Issued when execution context is destroyed.
 */
public class ExecutionContextDestroyedPayload {
    /**
     * Id of the destroyed context
     */
    private int executionContextId;

    public int getExecutionContextId() {
        return executionContextId;
    }

    public void setExecutionContextId(int executionContextId) {
        this.executionContextId = executionContextId;
    }
}
