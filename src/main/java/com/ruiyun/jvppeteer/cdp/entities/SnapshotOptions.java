package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.api.core.ElementHandle;

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

    /**
     * If true, gets accessibility trees for each of the iframes in the frame
     * subtree.
     */
    private boolean includeIframes;

    public SnapshotOptions() {
    }

    public SnapshotOptions(boolean interestingOnly, ElementHandle root, boolean includeIframes) {
        this.interestingOnly = interestingOnly;
        this.root = root;
        this.includeIframes = includeIframes;
    }

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

    public boolean getIncludeIframes() {
        return includeIframes;
    }

    public void setIncludeIframes(boolean includeIframes) {
        this.includeIframes = includeIframes;
    }
}
