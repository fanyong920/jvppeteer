package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.core.page.TargetInfo;

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
