package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;

/**
 * Issued when some information about a target has changed. This only happens between
 `targetCreated` and `targetDestroyed`.
 */
public class TargetInfoChangedEvent {

    private TargetInfo targetInfo;

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }

    public void setTargetInfo(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
    }
}
