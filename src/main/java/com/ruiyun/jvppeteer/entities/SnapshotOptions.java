package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.core.ElementHandle;

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
    private ElementHandle root;

    public boolean getInterestingOnly() {
        return interestingOnly;
    }

    public void setInterestingOnly(boolean interestingOnly) {
        this.interestingOnly = interestingOnly;
    }

    public ElementHandle getRoot() {
        return root;
    }

    public void setRoot(ElementHandle root) {
        this.root = root;
    }
}
