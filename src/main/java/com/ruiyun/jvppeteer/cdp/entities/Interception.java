package com.ruiyun.jvppeteer.cdp.entities;

import java.util.ArrayList;
import java.util.List;

public class Interception {
    private boolean enabled = false;
    private boolean handled = false;
    private List<Runnable> handlers = new ArrayList<>();
    private InterceptResolutionState resolutionState = new InterceptResolutionState();
    private ContinueRequestOverrides requestOverrides = new ContinueRequestOverrides();
    private ResponseForRequest response;
    private ErrorReasons abortReason;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ErrorReasons getAbortReason() {
        return abortReason;
    }

    public void setAbortReason(ErrorReasons abortReason) {
        this.abortReason = abortReason;
    }

    public ResponseForRequest getResponse() {
        return response;
    }

    public void setResponse(ResponseForRequest response) {
        this.response = response;
    }

    public ContinueRequestOverrides getRequestOverrides() {
        return requestOverrides;
    }

    public void setRequestOverrides(ContinueRequestOverrides requestOverrides) {
        this.requestOverrides = requestOverrides;
    }

    public InterceptResolutionState getResolutionState() {
        return resolutionState;
    }

    public void setResolutionState(InterceptResolutionState resolutionState) {
        this.resolutionState = resolutionState;
    }

    public List<Runnable> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<Runnable> handlers) {
        this.handlers = handlers;
    }

    public boolean getHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
