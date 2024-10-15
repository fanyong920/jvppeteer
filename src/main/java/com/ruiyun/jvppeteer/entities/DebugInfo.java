package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.exception.ProtocolException;

import java.util.List;

public class DebugInfo {
    private List<ProtocolException> pendingProtocolErrors;

    @Override
    public String toString() {
        return "DebugInfo{" +
                "pendingProtocolErrors=" + pendingProtocolErrors +
                '}';
    }

    public List<ProtocolException> getPendingProtocolErrors() {
        return pendingProtocolErrors;
    }

    public void setPendingProtocolErrors(List<ProtocolException> pendingProtocolErrors) {
        this.pendingProtocolErrors = pendingProtocolErrors;
    }

    public DebugInfo(List<ProtocolException> pendingProtocolErrors) {
        this.pendingProtocolErrors = pendingProtocolErrors;
    }
}
