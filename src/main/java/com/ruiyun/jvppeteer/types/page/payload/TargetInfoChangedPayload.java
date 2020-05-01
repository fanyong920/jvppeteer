package com.ruiyun.jvppeteer.types.page.payload;

import com.ruiyun.jvppeteer.protocol.target.TargetInfo;

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
