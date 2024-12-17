package com.ruiyun.jvppeteer.cdp.entities;

import com.ruiyun.jvppeteer.api.core.ElementHandle;

public class WaitForSelectorOptions {
    /**
     * 等待所选元素出现在 DOM 中并可见。有关元素可见性的定义，请参阅 ElementHandle.isVisible()。
     */
    private boolean visible;
    /**
     * 等待所选元素在 DOM 中不存在或被隐藏。有关元素不可见性的定义，请参阅 ElementHandle.isHidden()。
     */
    private boolean hidden;
    /**
     * Maximum time to wait in milliseconds. Defaults to `30000` (30 seconds).
     * Pass `0` to disable the timeout. Puppeteer's default timeout can be changed
     * using {@link com.ruiyun.jvppeteer.api.core.Page#setDefaultTimeout(int)}.
     */
    private Integer timeout;
    /**
     * An interval at which the `pageFunction` is executed, defaults to `raf`. If
     * `polling` is a number, then it is treated as an interval in milliseconds at
     * which the function would be executed. If `polling` is a string, then it can
     * be one of the following values:
     * <p>
     * - `raf` - to constantly execute `pageFunction` in `requestAnimationFrame`
     * callback. This is the tightest polling mode which is suitable to observe
     * styling changes.
     * <p>
     * - `mutation` - to execute `pageFunction` on every DOM mutation.
     */
    private String polling;

    private ElementHandle root;

    public WaitForSelectorOptions() {
        super();
    }

    public WaitForSelectorOptions(boolean visible, boolean hidden) {
        super();
        this.visible = visible;
        this.hidden = hidden;
    }

    public WaitForSelectorOptions(boolean visible, boolean hidden, String polling) {
        super();
        this.visible = visible;
        this.hidden = hidden;
        this.polling = polling;
    }

    public WaitForSelectorOptions(boolean visible, boolean hidden, Integer timeout, String polling) {
        super();
        this.visible = visible;
        this.hidden = hidden;
        this.timeout = timeout;
        this.polling = polling;
    }


    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getPolling() {
        return polling;
    }

    public void setPolling(String polling) {
        this.polling = polling;
    }

    public ElementHandle getRoot() {
        return root;
    }

    public void setRoot(ElementHandle root) {
        this.root = root;
    }
}
