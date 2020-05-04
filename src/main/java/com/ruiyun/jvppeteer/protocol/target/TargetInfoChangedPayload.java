package com.ruiyun.jvppeteer.protocol.target;

import com.ruiyun.jvppeteer.types.page.TargetInfo;

/**
 * Issued when some information about a target has changed. This only happens between
 `targetCreated` and `targetDestroyed`.
 */
public class TargetInfoChangedPayload {

    private TargetInfo targetInfo;

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }

    public void setTargetInfo(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
    }
}
