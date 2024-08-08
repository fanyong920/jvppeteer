package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.core.page.TargetInfo;

/**
 * Issued when a possible inspection target is created.
 */
public class TargetCreatedEvent {

    private TargetInfo targetInfo;

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }

    public void setTargetInfo(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
    }
}
