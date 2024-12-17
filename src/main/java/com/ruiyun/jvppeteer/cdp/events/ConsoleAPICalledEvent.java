package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.cdp.entities.StackTrace;

import java.math.BigDecimal;
import java.util.List;

/**
 * Issued when console API was called.
 */

public class ConsoleAPICalledEvent {
    /**
     * Type of the call
     * "log"|"debug"|"info"|"error"|"warning"|"dir"|"dirxml"|"table"|"trace"|"clear"|"startGroup"|"startGroupCollapsed"|"endGroup"|"assert"|"profile"|"profileEnd"|"count"|"timeEnd".
     */
    private String type;
    /**
     * Call arguments.
     */
    private List<RemoteObject> args;
    /**
     * Identifier of the context where the call was made.
     */
    private int executionContextId;
    /**
     * Call timestamp.
     */
    private BigDecimal timestamp;
    /**
     * Stack trace captured when the call was made. The async stack chain is automatically reported for
     * the following call types: `assert`, `error`, `trace`, `warning`. For other types the async call
     * chain can be retrieved using `Debugger.getStackTrace` and `stackTrace.parentId` field.
     */
    private StackTrace stackTrace;
    /**
     * Console context descriptor for calls on non-default console context (not console.*):
     * 'anonymous#unique-logger-id' for call on unnamed context, 'name#unique-logger-id' for call
     * on named context.
     */
    private String context;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<RemoteObject> getArgs() {
        return args;
    }

    public void setArgs(List<RemoteObject> args) {
        this.args = args;
    }

    public int getExecutionContextId() {
        return executionContextId;
    }

    public void setExecutionContextId(int executionContextId) {
        this.executionContextId = executionContextId;
    }

    public BigDecimal getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigDecimal timestamp) {
        this.timestamp = timestamp;
    }

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
