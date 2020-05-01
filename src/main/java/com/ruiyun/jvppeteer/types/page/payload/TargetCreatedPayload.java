package com.ruiyun.jvppeteer.types.page.payload;

import com.ruiyun.jvppeteer.protocol.target.TargetInfo;

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
