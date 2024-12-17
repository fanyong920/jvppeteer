package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import java.util.List;

public class WaitForOptions {
    /**
     * 最大等待时间（毫秒）。传递 0 以禁用超时。
     *
     * 默认值可以通过使用 {@link com.ruiyun.jvppeteer.api.core.Page#setDefaultTimeout} 或
     * {@link com.ruiyun.jvppeteer.api.core.Page#setDefaultNavigationTimeout} 方法进行更改。
     *
     */
    private int timeout = 30000;

    /**
     * 确定等待成功的条件。给定一个事件字符串数组，当所有事件都被触发时，等待被认为是成功的。
     *
     * @defaultValue `'load'`
     */
    private List<PuppeteerLifeCycle> waitUntil;

    /**
     * @internal
     */
    private boolean ignoreSameDocumentNavigation = false;



    // Getters and Setters
    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public List<PuppeteerLifeCycle> getWaitUntil() {
        return waitUntil;
    }

    public void setWaitUntil(List<PuppeteerLifeCycle> waitUntil) {
        this.waitUntil = waitUntil;
    }

    public boolean getIgnoreSameDocumentNavigation() {
        return ignoreSameDocumentNavigation;
    }

    public void setIgnoreSameDocumentNavigation(boolean ignoreSameDocumentNavigation) {
        this.ignoreSameDocumentNavigation = ignoreSameDocumentNavigation;
    }
}
