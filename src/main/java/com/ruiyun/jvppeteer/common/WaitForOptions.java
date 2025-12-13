package com.ruiyun.jvppeteer.common;

import java.util.List;

public class WaitForOptions {

    private boolean ignoreSameDocumentNavigation;

    /**
     * 什么时候要考虑等待成功。给定一组事件字符串，在所有事件被触发后，等待被认为是成功的。
     */
    private List<PuppeteerLifeCycle> waitUntil;
    /**
     * 最长等待时间（以毫秒为单位）。传递 0 以禁用超时。
     * 可以使用 Page.setDefaultTimeout() 或 Page.setDefaultNavigationTimeout() 方法更改默认值。
     */
    private Integer timeout;

    public boolean getIgnoreSameDocumentNavigation() {
        return ignoreSameDocumentNavigation;
    }

    public void setIgnoreSameDocumentNavigation(boolean ignoreSameDocumentNavigation) {
        this.ignoreSameDocumentNavigation = ignoreSameDocumentNavigation;
    }

    public List<PuppeteerLifeCycle> getWaitUntil() {
        return waitUntil;
    }

    public void setWaitUntil(List<PuppeteerLifeCycle> waitUntil) {
        this.waitUntil = waitUntil;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "WaitForOptions{" +
                "ignoreSameDocumentNavigation=" + ignoreSameDocumentNavigation +
                ", waitUntil=" + waitUntil +
                ", timeout=" + timeout +
                '}';
    }
}
