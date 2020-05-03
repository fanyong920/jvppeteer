package com.ruiyun.jvppeteer.protocol.target;

import com.ruiyun.jvppeteer.types.page.target.TargetInfo;

/**
 * Issued when a possible inspection target is created.
 */
public class TargetCreatedPayload {

    private TargetInfo targetInfo;

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }

    public void setTargetInfo(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
    }
}
