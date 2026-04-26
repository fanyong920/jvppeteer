package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.cdp.entities.WebMCPInvocationStatus;

/**
 * Result of a tool call
 */
public class WebMCPToolCallResult {
    /**
     * Tool invocation identifier.
     */
    private String id;

    /**
     * The corresponding tool call if available.
     */
    private WebMCPToolCall call;

    /**
     * Status of the invocation.
     */
    private WebMCPInvocationStatus status;

    /**
     * Output or error delivered as delivered to the agent. Missing if status is anything
     * other than Success.
     */
    private Object output;

    /**
     * Error text.
     */
    private String errorText;

    /**
     * The exception object, if the javascript tool threw an error.
     */
    private RemoteObject exception;

    public WebMCPToolCallResult() {
    }

    public WebMCPToolCallResult(String id, WebMCPToolCall call, WebMCPInvocationStatus status, Object output, String errorText, RemoteObject exception) {
        this.id = id;
        this.call = call;
        this.status = status;
        this.output = output;
        this.errorText = errorText;
        this.exception = exception;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WebMCPToolCall getCall() {
        return call;
    }

    public void setCall(WebMCPToolCall call) {
        this.call = call;
    }

    public WebMCPInvocationStatus getStatus() {
        return status;
    }

    public void setStatus(WebMCPInvocationStatus status) {
        this.status = status;
    }

    public Object getOutput() {
        return output;
    }

    public void setOutput(Object output) {
        this.output = output;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public RemoteObject getException() {
        return exception;
    }

    public void setException(RemoteObject exception) {
        this.exception = exception;
    }
}
