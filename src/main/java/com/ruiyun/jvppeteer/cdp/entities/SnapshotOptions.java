package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.cdp.core.CdpElementHandle;

public class SnapshotOptions {
    /**
     * 从树中修剪掉不感兴趣的节点。
     */
    private boolean interestingOnly = true;
    /**
     * 获取可访问性树的根节点
     * <p>
     * 整个页面的根节点
     */
    private CdpElementHandle root;

    public boolean getInterestingOnly() {
        return interestingOnly;
    }

    public void setInterestingOnly(boolean interestingOnly) {
        this.interestingOnly = interestingOnly;
    }

    public CdpElementHandle getRoot() {
        return root;
    }

    public void setRoot(CdpElementHandle root) {
        this.root = root;
    }
}
