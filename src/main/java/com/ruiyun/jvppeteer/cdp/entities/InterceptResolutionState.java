package com.ruiyun.jvppeteer.cdp.entities;

public class InterceptResolutionState {
    private InterceptResolutionAction action = InterceptResolutionAction.NONE;
    private Integer priority;

    public InterceptResolutionState() {
    }

    public InterceptResolutionState(InterceptResolutionAction action, Integer priority) {
        this.action = action;
        this.priority = priority;
    }

    public InterceptResolutionAction getAction() {
        return action;
    }

    public void setAction(InterceptResolutionAction action) {
        this.action = action;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
