package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.cdp.entities.WebMCPInvocationStatus;

public class ProtocolWebMCPToolRespondedEvent {
    /**
     * 调用标识符，与调用事件中的 invocationId 对应
     */
    private String invocationId;

    /**
     * 调用状态：Success、Canceled 或 Error
     */
    private WebMCPInvocationStatus status;

    /**
     * 输出结果（仅在状态为 Success 时存在）
     */
    private Object output;

    /**
     * 错误文本描述
     */
    private String errorText;

    /**
     * 异常对象（如果 JavaScript 工具抛出错误）
     * 对应 Protocol.Runtime.RemoteObject
     */
    private RemoteObject exception;

    public String getInvocationId() {
        return invocationId;
    }

    public void setInvocationId(String invocationId) {
        this.invocationId = invocationId;
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
