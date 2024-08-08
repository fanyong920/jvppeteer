package com.ruiyun.jvppeteer.protocol.runtime;

/**
 * Issued when execution context is destroyed.
 */
public class ExecutionContextDestroyedEvent {
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
